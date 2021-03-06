package com.java.mc.job.reader;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.thymeleaf.util.StringUtils;

import com.java.mc.bean.BatchJob;
import com.java.mc.bean.DatasourceConfig;
import com.java.mc.bean.MailServerConfig;
import com.java.mc.bean.SendCondition;
import com.java.mc.bean.ShortMessageConfiguration;
import com.java.mc.db.DBConnection;
import com.java.mc.db.DBOperation;
import com.java.mc.utils.CheckUtils;
import com.java.mc.utils.Constants;
import com.java.mc.utils.DBUtils;

@Configuration
public class BatchItemReader implements ItemReader<BatchJob> {
	private static final Logger logger = LoggerFactory.getLogger(BatchItemReader.class);

	@Autowired
	private DBOperation dbOperation;
	
	@Autowired
	private DBConnection dbConnection;

	private Map<String, Long> countMap = new HashMap<>();

	private List<BatchJob> batchJobList;

	@Override
	public BatchJob read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if (this.batchJobList == null) {
			this.batchJobList = this.dbOperation.getRunnableBatchSenderJobList();

			if (this.batchJobList.size() > 0) {
				// set configuration
				Iterator<BatchJob> it = this.batchJobList.iterator();
				List<BatchJob> failedList = new ArrayList<>();
				while (it.hasNext()) {
					BatchJob batchJob = it.next();
					try {
						this.set(batchJob);
					} catch (Exception e) {
						// remove invalid job.
						it.remove();
						this.setFailedStatus(batchJob, e.getMessage());
						failedList.add(batchJob);
						continue;
					}
					if (!this.validate(batchJob)) {
						it.remove();
					}
				}

				/// set the invalid job to failed status
				if (failedList.size() > 0) {
					this.dbOperation.updateScheduleJobStatus(failedList);
					this.updateRemoteFailedStatus(failedList);
				}
			}
		}

		if (this.batchJobList.isEmpty()) {
			this.countMap = null; // reset count map
			this.batchJobList = null;
		} else {
			return this.batchJobList.remove(0);
		}
		return null;
	}

	/**
	 * check limit regulations.
	 * 
	 * @param msconfig
	 * @return true if in ,other is out.
	 */
	private boolean validate(BatchJob batchJob) {

		if (this.countMap == null) {
			this.countMap = new HashMap<String, Long>();
		}

		if (batchJob == null) {
			return false;
		}

		int limitCount = 0, limitCycle = 0;
		String key = "";

		// send all the short message
		if (Constants.ACTION_MAIL_SCAN == batchJob.getActionType()) {
			if (batchJob.getMsConfig() == null) {
				return false;
			}

			limitCount = batchJob.getMsConfig().getLimitCount();
			limitCycle = batchJob.getMsConfig().getLimitCycle();
			key = new StringBuffer("ms-").append(batchJob.getMsConfig().getId()).toString();
		}
		if (Constants.ACTION_SM_SCAN == batchJob.getActionType()) {
			if (batchJob.getSmConfig() == null) {
				return false;
			}

			limitCount = batchJob.getSmConfig().getLimitCount();
			limitCycle = batchJob.getSmConfig().getLimitCycle();
			key = new StringBuffer("sm-").append(batchJob.getSmConfig().getId()).toString();
		}

		if (limitCount == 0 || limitCycle == 0) {
			return true;
		}

		Long count = countMap.get(key);
		if (count == null) {
			Calendar before = Calendar.getInstance();
			before.add(Calendar.MINUTE, -limitCycle);

			if (Constants.ACTION_MAIL_SCAN == batchJob.getActionType()) {
				count = this.dbOperation.getBatchMailJobCompleteCountByMSId(batchJob.getMsid(),
						new Timestamp(before.getTimeInMillis()));
			} else {
				count = this.dbOperation.getBatchSMJobCompleteCountBySMId(batchJob.getSmid(),
						new Timestamp(before.getTimeInMillis()));
			}
		}

		if (count >= limitCount) {
			return false;
		}
		countMap.put(key, ++count);
		return true;
	}

	private void set(BatchJob batchJob) throws Exception {
		List<SendCondition> scList = this.dbOperation.getSendConditionListByScheduleId(batchJob.getScheduleId());
		Integer handlerId = CheckUtils.getHanderId(batchJob, scList,
				Constants.ACTION_MAIL_SCAN == batchJob.getActionType() ? batchJob.getMsid()
						: Constants.ACTION_SM_SCAN == batchJob.getActionType() ? batchJob.getSmid() : null);
		this.set(batchJob, handlerId);
	}

	private void set(BatchJob batchJob, Integer targetId) throws Exception {
		if (targetId == null) {
			throw new Exception("任务处理程序未找到，或不匹配任何规则");
		}

		if (Constants.ACTION_MAIL_SCAN == batchJob.getActionType()) {
			MailServerConfig mc = this.dbOperation.getMailServerConfigrationById(targetId);
			if (mc == null) {
				throw new Exception("邮件服务器配置信息未找到");
			}
			batchJob.setMsConfig(mc);
			batchJob.setFromAddress(mc.getDefaultSenderAddress());
			if (StringUtils.isEmpty(batchJob.getSenderAddress()) || StringUtils.isEmpty(batchJob.getSenderTitle())
					|| StringUtils.isEmpty(batchJob.getSenderUserName())) {
				batchJob.setSenderAddress(mc.getDefaultSenderAddress());
				batchJob.setSenderTitle(mc.getDefaultSenderTitle());
				batchJob.setSenderUserName(mc.getDefaultSenderUserName());
				batchJob.setSenderPassword(mc.getDefaultSenderPassword());
			}
		}
		if (Constants.ACTION_SM_SCAN == batchJob.getActionType()) {
			ShortMessageConfiguration smc = this.dbOperation.getShortMessageConfigrationById(targetId);
			if (smc == null) {
				throw new Exception("短信通道配置信息未找到");
			}
			batchJob.setSmConfig(smc);
			batchJob.setFromAddress(new StringBuffer("").append(smc.getSmAccessNumber())
					.append(smc.getExtendCode() == null ? "" : smc.getExtendCode()).toString());
		}
	}

	private void setFailedStatus(BatchJob batchJob, String message) {
		if (batchJob != null) {
			batchJob.setStatus(Constants.SEND_END);
			batchJob.setCode(Constants.FAILED);
			batchJob.setMessage(message);
			batchJob.setSendTime(new Timestamp(System.currentTimeMillis()));
			logger.info("[warining][action=MatchSendCondition][result=ignore][id={}][seq={}][message={}]",
					batchJob.getJobId(), batchJob.getSeq(), message);
		}
	}
	
	private void updateRemoteFailedStatus(List<BatchJob> batchJobList) throws Exception{
		if(batchJobList != null && batchJobList.size() > 0){
			for(BatchJob batchJob : batchJobList){
				if(Constants.ACTION_MAIL_SCAN == batchJob.getActionType()){
					this.updateMailStatus(batchJob);
				}
				if(Constants.ACTION_SM_SCAN == batchJob.getActionType()){
					this.updateSMStatus(batchJob);
				}
			}
		}
	}

	private void updateMailStatus(BatchJob batchJob) throws Exception {
		if (batchJob != null) {
			DatasourceConfig dsc = this.dbOperation.getDSConfigurationById(batchJob.getDsid());
			if(dsc == null){
				throw new Exception("没有找到数据源配置信息，或者数据源配置信息已删除");
			}
			String remoteSQL = DBUtils.getSQL("UPDATE :t SET SEND = ?, ERROR_CODE = ? WHERE SEQ = ?", dsc.getSqlType(),
					dsc.getArchName(), Constants.WEBMAIL_V2);
			this.dbConnection.getRemoteJdbcTemplate(dsc.getId()).update(remoteSQL,
					new PreparedStatementSetter() {
						
						@Override
						public void setValues(PreparedStatement ps) throws SQLException {
							ps.setShort(1, batchJob.getStatus());
							ps.setShort(2, batchJob.getCode());
							ps.setLong(3, Long.valueOf(batchJob.getSeq()));
						}
					});
		}
	}
	
	private void updateSMStatus(BatchJob batchJob) throws Exception {
		if (batchJob != null) {
			DatasourceConfig dsc = this.dbOperation.getDSConfigurationById(batchJob.getDsid());
			if(dsc == null){
				throw new Exception("没有找到数据源配置信息，或者数据源配置信息已删除");
			}
			String remoteSQL = DBUtils.getSQL("UPDATE :t SET IS_SEND = ?, ERROE_CODE = ? WHERE MESSAGE_ID = ?",
					dsc.getSqlType(), dsc.getArchName(), Constants.WEB_SMS);
			this.dbConnection.getRemoteJdbcTemplate(dsc.getId()).update(remoteSQL,
					new PreparedStatementSetter() {

						@Override
						public void setValues(PreparedStatement ps) throws SQLException {
							ps.setShort(1, batchJob.getStatus());
							ps.setShort(2, batchJob.getCode());
							ps.setLong(3, Long.valueOf(batchJob.getSeq()));
						}
					});
		}
	}

}

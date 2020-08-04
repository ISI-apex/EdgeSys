package workloads.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.storm.Constants;
import com.twitter.heron.api.tuple.Tuple;

import java.util.HashMap;

public class TupleInfo {
	private final Logger LOG =
			LoggerFactory.getLogger(TupleInfo.class);
	private HashMap<Object, Long> tupleData;

	public TupleInfo() {
		tupleData = new HashMap<>();
	}

	public void logStartTime(Object messageId) {
		if(this.tupleData.containsKey(messageId)) {
			LOG.warn("Trying to log message twice: " + messageId);
		} else {
			LOG.trace("Storing start time for " + messageId);
			// this.tupleData.put(messageId, System.nanoTime());
			this.updateStartTime(messageId, System.nanoTime());
		}
	}

	public void updateStartTime(Object messageId, Long startTime) {
		this.tupleData.put(messageId, startTime);
	}

	public Long getStartTime(Object messageId) {
		Long returnTime;
		if(this.tupleData.containsKey(messageId)) {
			LOG.trace("Returning found start time " + messageId);
			returnTime = this.tupleData.get(messageId);
			this.tupleData.remove(messageId);
			return returnTime;
		} else {
			LOG.warn("Trying to get start time that doesn't exist: " + messageId);
			return null;
		}
	}

	public boolean logAndCheckIfProcessed(Object tupleId) {
		if(tupleData.containsKey(tupleId)) {
			tupleData.remove(tupleId);
			return false;
		} else {
			tupleData.put(tupleId, null);
			return true;
		}
	}

	public static boolean isTickTuple(Tuple tuple) {
		return tuple.getSourceComponent().equals(Constants.SYSTEM_COMPONENT_ID) && tuple.getSourceStreamId().equals(
				Constants.SYSTEM_TICK_STREAM_ID);
	}

}

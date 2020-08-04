package workloads.util;

import com.twitter.heron.api.spout.IRichSpout;
import com.twitter.heron.api.spout.SpoutOutputCollector;
import com.twitter.heron.api.topology.IUpdatable;
import com.twitter.heron.api.topology.OutputFieldsDeclarer;
import com.twitter.heron.api.topology.TopologyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edgesys.util.FileUtils;
import scala.annotation.tailrec;

import com.twitter.heron.api.spout.ISpoutOutputCollector;
import com.twitter.heron.api.utils.Utils;
import org.json.simple.JSONObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.List;



// Used in LibraryPercentile
// import org.apache.commons.math3.stat.descriptive.rank.Percentile;

// Used in CustomPercentile
import java.util.Collections;

import com.twitter.heron.api.metric.ReducedMetric;
import com.twitter.heron.api.metric.MeanReducer;
import com.twitter.heron.api.metric.MeanReducerState;
import com.twitter.heron.api.metric.IReducer;
import java.util.ArrayList;



// import dynamo.tools.*;
// import tools.FileUtils;

public final class MetricSpout implements IRichSpout, IUpdatable {
	/**
     *
     */
    private static final long serialVersionUID = -8267285032298725265L;

    private static final Logger LOG =
			LoggerFactory.getLogger(MetricSpout.class);

	// Delegate bolt for user-code
	private IRichSpout delegate;

	// Delegated SpoutOutputCollector
	private MetricSpoutOutputCollector metricSpoutOutputCollector;

	// Metrics tools for logging/recording metrics
	// private MetricsTools metricsTools;

	// Time for next emit(used when throttled)
	private Long nextEmitTime = null;
    private Long nextTupleDelayUs=null;
    

    private Long tuplesAcked=0l;
    private TupleInfo tupleInfo;
    private JSONObject tempJSONObj;
    private String outputFileName = "metrics-0.txt";
    private TopologyContext topologyContext;

	public MetricSpout(IRichSpout delegate, int sendDelayMs) {
		this.delegate = delegate;
	}



	@Override
	public void open(Map<String, Object> conf, TopologyContext topologyContext, SpoutOutputCollector spoutOutputCollector) {
        this.topologyContext = topologyContext;
		// Create metrics file
        openMetricsFile();
        
        tupleInfo = new TupleInfo();

		// Create delegated SpoutOutputCollector
		metricSpoutOutputCollector = new MetricSpoutOutputCollector(spoutOutputCollector, this.tupleInfo);

		// Call user open
		delegate.open(conf, topologyContext, metricSpoutOutputCollector);
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public void activate() {
		delegate.activate();
	}

	@Override
	public void deactivate() {
		delegate.deactivate();
	}

	@Override
	public void nextTuple() {
		// System.out.println(this.getClass().getName()+" nextTuple");
		if(this.nextEmitTime != null) {
			if(System.nanoTime() < this.nextEmitTime) {
				// System.out.println("Not time yet");
				return;
			} else {
				this.nextEmitTime = System.nanoTime() + this.nextTupleDelayUs * (long)1e3;
			}
		}
		// Update modes if necessary
		delegate.nextTuple();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void ack(Object msgId) {
        // this.metricsTools.ackTuple(msgId);
        ackTuple(msgId);
		delegate.ack(msgId);
	}

	@Override
	public void fail(Object o) {
		// this.metricsTools.failTuple(o);
		delegate.fail(o);
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
		delegate.declareOutputFields(outputFieldsDeclarer);
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return delegate.getComponentConfiguration();
	}

	@Override
	public void update(TopologyContext topologyContext) {
		if(this.delegate instanceof IUpdatable) {
			((IUpdatable)this.delegate).update(topologyContext);
		}
	}


    public void ackTuple(Object msgId) {
        this.tuplesAcked++;
        Object tmpMsgId = msgId;

        Long completeLatency;
        Long endTime = System.nanoTime();
        Long startTime = this.tupleInfo.getStartTime(msgId);
        LOG.info("Got ack for " + msgId + "\tcurrent: " + this.tuplesAcked);
        // if(this.kafkaOffsetMsgID.containsValue(msgId)) {
        // 	LOG.info("Also known as kafkaIdentifier " + this.kafkaOffsetMsgID.inverse().get(msgId));
        // 	tmpMsgId = this.kafkaOffsetMsgID.inverse().remove(msgId);
        // }
        if(startTime == null ) {
            return;
        }
        completeLatency=endTime-startTime;
        // Convert to ms and update metrics
        // latencyMetrics.ackTuple(completeLatency/1e6);
        this.tempJSONObj.clear();
        this.tempJSONObj.put("t", "ti");
        String tupleId=tmpMsgId.toString();
        String[] splitId=tupleId.split("@");
        String idToStore=splitId[splitId.length-1];
        this.tempJSONObj.put("id", idToStore);
        this.tempJSONObj.put("st", startTime);
        // tempJSONObj.put("endTime", endTime);
        // tempJSONObj.put("tupleId", tmpMsgId.toString());
        this.tempJSONObj.put("tl", completeLatency);
        FileUtils.writeToFile(outputFileName, tempJSONObj.toJSONString()+"\n");
    }


    public void openMetricsFile() {
        // Write out initial data
        outputFileName=String.format("metrics.TXT-%s", this.topologyContext.getThisTaskIndex());
        tempJSONObj = new JSONObject();
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        tempJSONObj.put("Date", dateFormat.format(date).toString());
        FileUtils.writeToFile(outputFileName, tempJSONObj.toJSONString()+"\n");
    }

	public static final class MetricSpoutOutputCollector
			extends SpoutOutputCollector {

		private ISpoutOutputCollector delegate;
        // private MetricsTools metricsTools;
        private TupleInfo tupleInfo;

		public MetricSpoutOutputCollector(
				ISpoutOutputCollector delegate,
                TupleInfo tupleInfo
                ) {
			super(delegate);
			this.delegate = delegate;
            // this.metricsTools = metricsTools;
            this.tupleInfo = tupleInfo;
		}

		@Override
		public List<Integer> emit(String streamId, List<Object> tuple,
															Object messageId) {
            this.tupleInfo.logStartTime(messageId);
			return this.delegate.emit(streamId, tuple, messageId);
		}

		@Override
		public void emitDirect(int taskId, String streamId, List<Object> tuple,
													 Object messageId) {
			delegate.emitDirect(taskId, streamId, tuple, messageId);
		}

		@Override
		public void reportError(Throwable error) {
			delegate.reportError(error);
		}


		// Extra methods from SpoutOutputCollector
		public List<Integer> emit(List<Object> tuple, Object messageId) {
			return emit(Utils.DEFAULT_STREAM_ID, tuple, messageId);
		}

		public List<Integer> emit(List<Object> tuple) {
			return emit(tuple, null);
		}

		public List<Integer> emit(String streamId, List<Object> tuple) {
			return emit(streamId, tuple, null);
		}

		public void emitDirect(int taskId, List<Object> tuple, Object messageId) {
			emitDirect(taskId, Utils.DEFAULT_STREAM_ID, tuple, messageId);
		}

		public void emitDirect(int taskId, String streamId, List<Object> tuple) {
			emitDirect(taskId, streamId, tuple, null);
		}

		public void emitDirect(int taskId, List<Object> tuple) {
			emitDirect(taskId, tuple, null);
		}
	}


}

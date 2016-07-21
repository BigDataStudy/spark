package edu.bigdata.bigdata.hbase;

import java.text.SimpleDateFormat;  
import java.util.Date;  
  

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Put;  
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat;  
import org.apache.hadoop.hbase.mapreduce.TableReducer;  
import org.apache.hadoop.hbase.util.Bytes;  
import org.apache.hadoop.io.LongWritable;  
import org.apache.hadoop.io.NullWritable;  
import org.apache.hadoop.io.Text;  
import org.apache.hadoop.mapreduce.Counter;  
import org.apache.hadoop.mapreduce.Job;  
import org.apache.hadoop.mapreduce.Mapper;  
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;  
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat; 

public class MapReduce_HbaseBatchImport_noreduce {  
  
    public static void main(String[] args) throws Exception {  
        final Configuration configuration = HBaseConfiguration.create();
    
        // 设置hbase表名称  
        configuration.set(TableOutputFormat.OUTPUT_TABLE, "student1");  
  
        // 将该值改大，防止hbase超时退出  
        configuration.set("dfs.socket.timeout", "180000");  
  
        final Job job = new Job(configuration);
        job.setJobName("HBaseBatchImport");
  
        job.setMapperClass(BatchImportMapper.class);  
        // 设置map的输出，不设置reduce的输出类型  
        job.setMapOutputKeyClass(LongWritable.class);  
        job.setMapOutputValueClass(Put.class);  
  
        job.setInputFormatClass(TextInputFormat.class);  
        // 不再设置输出路径，而是设置输出格式类型  
        job.setOutputFormatClass(TableOutputFormat.class);  
        
        job.setNumReduceTasks(0);
  
        FileInputFormat.setInputPaths(job, "hdfs://namenode02:9000/user/bigdata/hbasetest/student");  
        
        TableMapReduceUtil.addDependencyJars(job);
        
        job.waitForCompletion(true);  
    }  
  
    static class BatchImportMapper extends  
            Mapper<LongWritable, Text, LongWritable, Put> {  
        
        Text v2 = new Text();  
  
        protected void map(LongWritable key, Text value, Context context)  
                throws java.io.IOException, InterruptedException {  
            final String[] splited = value.toString().split("\t");  
            try {  
  
                final Put put = new Put(Bytes.toBytes(splited[0]));  
                put.addColumn(Bytes.toBytes("personal"), Bytes.toBytes("name"), Bytes  
                        .toBytes(splited[1]));  
                
                put.addColumn(Bytes.toBytes("school"), Bytes.toBytes("name"), Bytes  
                        .toBytes(splited[2]));  
                context.write(key, put); 
            } catch (NumberFormatException e) {  
                final Counter counter = context.getCounter("BatchImport",  
                        "ErrorFormat");  
                counter.increment(1L);  
                System.out.println("出错了" + splited[0] + " " + e.getMessage());  
            }  
        };  
    }  
  
  
}  
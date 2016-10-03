
package fi.tut.dip;

import java.io.IOException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

import java.util.regex.*;
import java.io.*;
import java.nio.charset.Charset;



public class ProductCount {

    public static class TokenizerMapper
            extends Mapper<LongWritable, Text, Text, LongWritable>{

        private static final String logEntryPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:\\/]+\\s[+\\-]\\d{4})\\] \"(.+?)\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
        private final static LongWritable one = new LongWritable(1);


        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String txt = value.toString();

            Pattern pattern = Pattern.compile(logEntryPattern);
            Matcher matcher = pattern.matcher(txt);
            while (matcher.find()) {
                //3ICE> group 5 is for example "GET /department/apparel/category/featured%20shops/product/adidas%20Kids'%20RG%20III%20Mid%20Football%20Cleat HTTP/1.1"
                context.write(new Text(matcher.group(5)), one);
            }
        }

    }

    public static class IntSumReducer
            extends Reducer<Text,LongWritable,Text,LongWritable> {

        protected void reduce(Text key, Iterable<LongWritable> values, Context context)
                throws IOException, InterruptedException {
            long sum = 0;
            for (LongWritable value : values) {
                sum = sum + value.get();
            }
            context.write(key, new LongWritable(sum));
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();

        if (otherArgs.length < 2) {
            System.err.println("Usage: ProductCount <in> [<in>...] <out>");
            System.exit(2);
        }

        Job job = Job.getInstance(conf, "Product Count");

        job.setJarByClass(ProductCount.class);
        job.setMapperClass(TokenizerMapper.class);
        job.setReducerClass(IntSumReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(LongWritable.class);
        //3ICE: fix Type mismatch
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(LongWritable.class);
        
        for (int i = 0; i < otherArgs.length - 1; ++i) {
            TextInputFormat.addInputPath(job, new Path(otherArgs[i]));
        }

        FileOutputFormat.setOutputPath(job,new Path(otherArgs[otherArgs.length - 1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

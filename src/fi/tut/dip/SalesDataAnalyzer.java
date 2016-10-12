
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

import java.net.URLDecoder;
import java.util.regex.*;


public class SalesDataAnalyzer {

    public static class TokenizerMapper
            extends Mapper<LongWritable, Text, Text, LongWritable>{

        private static final String logEntryPattern = "^([\\d.]+) (\\S+) (\\S+) \\[([\\w:\\/]+\\s[+\\-]\\d{4})\\] \"GET /department/(.+?) HTTP/1.1\" (\\d{3}) (\\d+) \"([^\"]+)\" \"([^\"]+)\"";
        private final static LongWritable one = new LongWritable(1);


        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String txt = value.toString();

            Pattern pattern = Pattern.compile(logEntryPattern);
            Matcher matcher = pattern.matcher(txt);
            while (matcher.find()) {
                // group 5 is for example "apparel/category/featured%20shops/product/adidas%20Kids'%20RG%20III%20Mid%20Football%20Cleat"
                // when it's a product. There are also requests ending with .../add_to_cart,
                // that we should ignore (they add to cart using GET, not really smart but that's not our concern...)
                String url = matcher.group(5);
                String[] parts = url.split("/");

                // should be 5 parts, any more and it's add_to_cart, any less and it's a department or category
                if (parts.length == 5) {
                    String product = decode(parts[4]);
                    //String category = decode(parts[2]);
                    //String department = decode(parts[0]);

                    context.write(new Text(product), one);
                }
            }
        }

        public static String decode(String s) {
            try {
                return URLDecoder.decode(s, "UTF-8");
            } catch (Throwable t) { return null; }
        }
    }


    public static class HourMapper
            extends Mapper<LongWritable, Text, Text, LongWritable> {

        private final static LongWritable one = new LongWritable(1);


        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            // the values should be line separated already, but lets just make sure we handle multiple lines eithe way
            String[] lines = value.toString().split("\\n");

            for (String line : lines) {
                // lines start like:
                // 9.133.215.123 - - [14/Jun/2014:10:30:13 -0400] ...
                // we only care
                String timestamp = line.substring(line.indexOf("["), line.indexOf("]"));
                String hour = line.split(":")[1];

                context.write(new Text(hour), one);
            }

            // log lines start at
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

        if (otherArgs.length < 3) {
            System.err.println("Usage: SalesDataAnalyzer <in> [<in>...] <out1> <out2>");
            System.exit(2);
        }

        Job productCountJob = Job.getInstance(conf, "Product Count");
        Job hourJob = Job.getInstance(conf, "Hour Count");


        productCountJob.setJarByClass(SalesDataAnalyzer.class);
        productCountJob.setMapperClass(TokenizerMapper.class);
        productCountJob.setReducerClass(IntSumReducer.class);
        productCountJob.setOutputKeyClass(Text.class);
        productCountJob.setOutputValueClass(LongWritable.class);
        //3ICE: fix Type mismatch
        productCountJob.setMapOutputKeyClass(Text.class);
        productCountJob.setMapOutputValueClass(LongWritable.class);
        
        for (int i = 0; i < otherArgs.length - 2; ++i) {
            TextInputFormat.addInputPath(productCountJob, new Path(otherArgs[i]));
        }

        FileOutputFormat.setOutputPath(productCountJob,new Path(otherArgs[otherArgs.length - 2]));

        hourJob.setJarByClass(SalesDataAnalyzer.class);
        hourJob.setMapperClass(HourMapper.class);
        hourJob.setReducerClass(IntSumReducer.class);
        hourJob.setOutputKeyClass(Text.class);
        hourJob.setOutputValueClass(LongWritable.class);
        //3ICE: fix Type mismatch
        hourJob.setMapOutputKeyClass(Text.class);
        hourJob.setMapOutputValueClass(LongWritable.class);

        for (int i = 0; i < otherArgs.length - 2; ++i) {
            TextInputFormat.addInputPath(hourJob, new Path(otherArgs[i]));
        }

        FileOutputFormat.setOutputPath(hourJob, new Path(otherArgs[otherArgs.length - 1]));


        System.exit(
                (productCountJob.waitForCompletion(true) &&
                hourJob.waitForCompletion(true))
                ? 0 : 1);
    }

}

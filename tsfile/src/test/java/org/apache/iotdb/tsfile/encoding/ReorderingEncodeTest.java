package org.apache.iotdb.tsfile.encoding;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.sort;

public class ReorderingEncodeTest {

  static int DeviationOutlierThreshold = 8;
  static int OutlierThreshold = 0;
  public static int zigzag(int num){
    if(num<0){
      return 2*(-num)-1;
    }else{
      return 2*num;
    }
  }

  public static int max3(int a,int b,int c){
    if(a>=b && a >=c){
      return a;
    }else if(b >= a && b >= c){
      return b;
    }else{
      return c;
    }
  }
  public static int getBitWith(int num){
    return 32 - Integer.numberOfLeadingZeros(num);
  }
  public static byte[] int2Bytes(int integer)
  {
    byte[] bytes=new byte[4];
    bytes[3]= (byte) ((byte) integer>>24);
    bytes[2]= (byte) ((byte) integer>>16);
    bytes[1]= (byte) ((byte) integer>>8);
    bytes[0]=(byte) integer;
    return bytes;
  }
  public static byte[] bitPacking(ArrayList<Integer> numbers,int bit_width){
    int block_num = numbers.size()/8;
    byte[] result = new byte[bit_width*block_num];
    for(int i=0;i<block_num;i++){
      for(int j=0;j<bit_width;j++){
        int tmp_int = 0;
        for(int k=0;k<8;k++){
          tmp_int += (((numbers.get(i*8+k) >>j) %2) << k);
        }
//             System.out.println(Integer.toBinaryString(tmp_int));
        result[i*bit_width+j] = (byte) tmp_int;
      }
    }
    return result;
  }
  public static byte[] bitPacking(ArrayList<ArrayList<Integer>> numbers,int index,int bit_width){
    int block_num = numbers.size()/8;
    byte[] result = new byte[bit_width*block_num];
    for(int i=0;i<block_num;i++){
      for(int j=0;j<bit_width;j++){
        int tmp_int = 0;
        for(int k=0;k<8;k++){
          tmp_int += (((numbers.get(i*8+k).get(index) >>j) %2) << k);
        }
//        System.out.println(Integer.toBinaryString(tmp_int));
        result[i*bit_width+j] = (byte) tmp_int;
      }
    }
    return result;
  }
  public static void quickSort(ArrayList<ArrayList<Integer>> ts_block, int index, int low, int high) {
    if(low>=high)
      return;
    ArrayList<Integer> pivot = ts_block.get(low);
    int l = low;
    int r = high;
    ArrayList<Integer> temp;
    while(l<r){
      while (l < r && ts_block.get(r).get(index) >= pivot.get(index)) {
        r--;
      }
      while (l < r && ts_block.get(l).get(index) <= pivot.get(index)) {
        l++;
      }
      if (l < r) {
        temp = ts_block.get(l);
        ts_block.set(l, ts_block.get(r));
        ts_block.set(r, temp);
      }
    }
    ts_block.set(low, ts_block.get(l));
    ts_block.set(l, pivot);
    if (low < l) {
      quickSort(ts_block,index, low, l - 1);
    }
    if (r < high) {
      quickSort(ts_block,index, r + 1, high);
    }
  }

  public static void splitTimeStamp(ArrayList<ArrayList<Integer>> ts_block, int block_size, int td,
                                    ArrayList<Integer> deviation_list,ArrayList<Integer> result){
    int deviation_max = Integer.MIN_VALUE;
    int max_bit_width_deviation=0;
    int r0 = 0;

    // split timestamp into intervals and deviations

    //address other timestamps and values

    for(int j=block_size-1;j>0;j--) {
      int delta_interval = (ts_block.get(j).get(0) - ts_block.get(j-1).get(0))/td;
      int deviation = (ts_block.get(j).get(0) - ts_block.get(j-1).get(0))%td;
      if(deviation >= (td/2)){
        deviation -= td;
        delta_interval ++;
      }
      deviation = zigzag(deviation);
      deviation_list.add(deviation);
      if(deviation > deviation_max){
        deviation_max = deviation;
      }

      int value = ts_block.get(j).get(1);
      ArrayList<Integer> tmp = new ArrayList<>();
      tmp.add(delta_interval);
      tmp.add(value);
      ts_block.set(j,tmp);
    }


    // address timestamp0
    r0 = ts_block.get(0).get(0) /td;
    int deviation0 = ts_block.get(0).get(0) %td;
    if(deviation0 >= (td/2)){
      deviation0 -= td;
      r0 ++;
    }
    deviation0 = zigzag(deviation0);
    deviation_list.add(deviation0);
    if(deviation0 > deviation_max){
      deviation_max = deviation0;
    }

    int value0 = ts_block.get(0).get(1);
    ArrayList<Integer> tmp0 = new ArrayList<>();
    tmp0.add(0);
    tmp0.add(value0);
    ts_block.set(0,tmp0);

    for(int j=1;j<block_size-1;j++){
      int interval = ts_block.get(j).get(0) + ts_block.get(j-1).get(0);
      int value = ts_block.get(j).get(1);
      ArrayList<Integer> tmp = new ArrayList<>();
      tmp.add(interval);
      tmp.add(value);
      ts_block.set(j,tmp);
    }
    max_bit_width_deviation = getBitWith(deviation_max);
    result.add(max_bit_width_deviation);
    result.add(r0);
  }
  public static ArrayList<ArrayList<Integer>> getEncodeBitsDelta(ArrayList<ArrayList<Integer>> ts_block, int block_size,
                                                      ArrayList<Integer> result, ArrayList<Integer> i_star){
    int timestamp_delta_min = Integer.MAX_VALUE;
    int value_delta_min = Integer.MAX_VALUE;
    ArrayList<ArrayList<Integer>> ts_block_delta = new ArrayList<>();

    ArrayList<Integer> tmp0 = new ArrayList<>();
    tmp0.add(ts_block.get(0).get(0));
    tmp0.add(ts_block.get(0).get(1));
    ts_block_delta.add(tmp0);

    // delta to Delta
    for(int j=1;j<block_size;j++) {
      int delta_r = ts_block.get(j).get(0) - ts_block.get(j-1).get(0);
      int delta_v = ts_block.get(j).get(1) - ts_block.get(j-1).get(1);
      ArrayList<Integer> tmp = new ArrayList<>();
      tmp.add(delta_r);
      tmp.add(delta_v);
      ts_block_delta.add(tmp);

      if(delta_r <timestamp_delta_min){
        timestamp_delta_min = delta_r;
      }
      if(delta_v < value_delta_min){
        value_delta_min = delta_v;
      }
    }

    int max_timestamp = Integer.MIN_VALUE;
    int max_timestamp_i = -1;
    int max_value = Integer.MIN_VALUE;
    int max_value_i = -1;
    for(int j=block_size-1;j>0;j--) {
      int ts_2diff_ts = ts_block_delta.get(j).get(0) - timestamp_delta_min;
      int ts_2diff_value = ts_block_delta.get(j).get(1) - value_delta_min;
      ArrayList<Integer> tmp = new ArrayList<>();
      tmp.add(ts_2diff_ts);
      tmp.add(ts_2diff_value);
      ts_block_delta.set(j,tmp);
      if(ts_2diff_ts>max_timestamp){
        max_timestamp = ts_2diff_ts;
        max_timestamp_i = j;
      }
      if(ts_2diff_value>max_value){
        max_value = ts_2diff_value;
        max_value_i = j;
      }
    }
    int max_bit_width_interval = getBitWith(max_timestamp);
    int max_bit_width_value = getBitWith(max_value);
    

    // calculate error
    int  length = (max_bit_width_interval+max_bit_width_value)*(block_size-1);
    result.add(length);
    result.add(max_bit_width_interval);
    result.add(max_bit_width_value);

    result.add(timestamp_delta_min);
    result.add(value_delta_min);

    i_star.add(max_timestamp_i);
    i_star.add(max_value_i);

    return ts_block_delta;
  }
  public static boolean adjustPoint(ArrayList<ArrayList<Integer>> ts_block, int i_star, int block_size,
                                    ArrayList<Integer> raw_length, int index, int j_star, int j_star_bit_width){
    j_star_bit_width = 33;
    j_star = 0;
    int delta_r_i_star_plus_1 = ts_block.get(i_star+1).get(0) - ts_block.get(i_star-1).get(0);
    int delta_v_i_star_plus_1 = ts_block.get(i_star+1).get(1) - ts_block.get(i_star-1).get(1);
    if(delta_r_i_star_plus_1 > raw_length.get(1) || delta_v_i_star_plus_1 >raw_length.get(2))
      return false;
    for(int j = 1;j<block_size;j++){
      if(j!=i_star){
        int delta_r_j = ts_block.get(j).get(0) - ts_block.get(i_star).get(0);
        int delta_v_j = ts_block.get(j).get(1) - ts_block.get(i_star).get(1);
        int delta_r_i_star = ts_block.get(i_star).get(0) - ts_block.get(j-1).get(0);
        int delta_v_i_star = ts_block.get(i_star).get(1) - ts_block.get(j-1).get(1);
        if(delta_r_j > raw_length.get(1) || delta_v_j >raw_length.get(2) ||
                delta_r_i_star > raw_length.get(1) || delta_v_i_star >raw_length.get(2)  )
          return false;
        int max_r = max3(delta_r_i_star_plus_1,delta_r_j,delta_r_i_star);
        int max_v = max3(delta_v_i_star_plus_1,delta_v_j,delta_v_i_star);
        // adjust r
        if(index == 1){
          if(max_v<=raw_length.get(2) && max_r < j_star_bit_width && max_r < raw_length.get(1)){
            j_star_bit_width = max_r;
            j_star = j;
          }
        }else{
          if(max_v<raw_length.get(2) && max_v < j_star_bit_width && max_r <= raw_length.get(1)){
            j_star_bit_width = max_v;
            j_star = j;
          }
        }

      }
    }
    return j_star != 0;
  }
  public static ArrayList<Byte> encode2Bytes(ArrayList<ArrayList<Integer>> ts_block,ArrayList<Integer> deviation_list,ArrayList<Integer> raw_length){
    ArrayList<Byte> encoded_result = new ArrayList<>();
    // encode block size (Integer)
    byte[] block_size_byte = int2Bytes(ts_block.size());
    for (byte b : block_size_byte) encoded_result.add(b);

    // r0 of a block (Integer)
    byte[] r0_byte = int2Bytes(raw_length.get(4));
    for (byte b : r0_byte) encoded_result.add(b);

    // encode min_delta_interval and min_delta_value
    byte[] min_delta_interval_byte = int2Bytes(raw_length.get(3));
    for (byte b : min_delta_interval_byte) encoded_result.add(b);
    byte[] min_delta_value_byte = int2Bytes(raw_length.get(4));
    for (byte b : min_delta_value_byte) encoded_result.add(b);

    // encode interval0 and value0
    byte[] interval0_byte = int2Bytes(ts_block.get(0).get(0));
    for (byte b : interval0_byte) encoded_result.add(b);
    byte[] value0_byte = int2Bytes(ts_block.get(0).get(1));
    for (byte b : value0_byte) encoded_result.add(b);


    // encode interval
    byte[] max_bit_width_interval_byte = int2Bytes(raw_length.get(1));
    for (byte b : max_bit_width_interval_byte) encoded_result.add(b);
    byte[] timestamp_bytes = bitPacking(ts_block,0,raw_length.get(1));
    for (byte b : timestamp_bytes) encoded_result.add(b);

    // encode value
    byte[] max_bit_width_value_byte = int2Bytes(raw_length.get(2));
    for (byte b : max_bit_width_value_byte) encoded_result.add(b);
    byte[] value_bytes = bitPacking(ts_block,1,raw_length.get(1));
    for (byte b : value_bytes) encoded_result.add(b);


    // encode deviation
    byte[] max_bit_width_deviation_byte = int2Bytes(raw_length.get(5));
    for (byte b: max_bit_width_deviation_byte) encoded_result.add(b);
    byte[] deviation_list_bytes = bitPacking(deviation_list,raw_length.get(3));
    for (byte b: deviation_list_bytes) encoded_result.add(b);


    return encoded_result;
  }
  public static ArrayList<Byte> ReorderingDeltaEncoder(ArrayList<ArrayList<Integer>> data,int block_size, int td){
    block_size ++;
    int length_all = data.size();
    int block_num = length_all/block_size;
    ArrayList<Byte> encoded_result=new ArrayList<Byte>();

    for(int i=0;i<1;i++){
//    for(int i=0;i<block_num;i++){
      ArrayList<ArrayList<Integer>> ts_block = new ArrayList<>();
      ArrayList<ArrayList<Integer>> ts_block_reorder = new ArrayList<>();
      for(int j=0;j<block_size;j++){
        ts_block.add(data.get(j+i*block_size));
        ts_block_reorder.add(data.get(j+i*block_size));
      }

      ArrayList<Integer> deviation_list = new ArrayList<>();
      ArrayList<Integer> result = new ArrayList<>();
      splitTimeStamp(ts_block,block_size,td,deviation_list,result);
      quickSort(ts_block,0,0,block_size-1);

      //ts_block order by interval

      // time-order
      ArrayList<Integer> raw_length = new ArrayList<>(); // length,max_bit_width_interval,max_bit_width_value,max_bit_width_deviation
      ArrayList<Integer> i_star_ready = new ArrayList<>();

      ArrayList<ArrayList<Integer>> ts_block_delta = getEncodeBitsDelta( ts_block,  block_size, raw_length,i_star_ready);
      raw_length.add(result.get(0)); // max_bit_width_deviation
      raw_length.add(result.get(1)); // r0

      // value-order
      quickSort(ts_block,1,0,block_size-1);
      ArrayList<Integer> reorder_length = new ArrayList<>();
      ArrayList<Integer> i_star_ready_reorder = new ArrayList<>();
      ArrayList<ArrayList<Integer>> ts_block_delta_reorder = getEncodeBitsDelta( ts_block,  block_size, reorder_length,i_star_ready_reorder);

      if(raw_length.get(0)<=reorder_length.get(0)){
        quickSort(ts_block,0,0,block_size-1);
        int i_star = i_star_ready.get(1);
        int j_star = 0;
        int j_star_bit_width = 33;
//        int raw_bit_width_r = raw_length.get(1);
        while(adjustPoint(ts_block,i_star,block_size,raw_length,0, j_star,j_star_bit_width)){
          ArrayList<Integer> tmp_tv = ts_block_reorder.get(i_star);
          if(j_star<i_star){
            for(int u=i_star-1;u>=j_star;u--){
              ArrayList<Integer> tmp_tv_cur = new ArrayList<>();
              tmp_tv_cur.add(ts_block_reorder.get(u).get(0));
              tmp_tv_cur.add(ts_block_reorder.get(u).get(1));
              ts_block.set(u+1,tmp_tv_cur);
            }
          }else{
            for(int u=i_star+1;u<=j_star;u++){
              ArrayList<Integer> tmp_tv_cur = new ArrayList<>();
              tmp_tv_cur.add(ts_block_reorder.get(u).get(0));
              tmp_tv_cur.add(ts_block_reorder.get(u).get(1));
              ts_block.set(u-1,tmp_tv_cur);
            }
          }
          ts_block.set(j_star,tmp_tv);
//          raw_bit_width_r = j_star_bit_width;
        }

        ts_block_delta_reorder = getEncodeBitsDelta( ts_block,  block_size,reorder_length,i_star_ready_reorder);
        ArrayList<Byte> cur_encoded_result = encode2Bytes(ts_block_delta_reorder,deviation_list,reorder_length);
        encoded_result.addAll(cur_encoded_result);

      }else{
        // adjust to reduce max_bit_width_r
        int i_star = i_star_ready_reorder.get(0);
        int j_star = 0;
        int j_star_bit_width = 33;
//        int raw_bit_width_r = raw_length.get(2);
        while(adjustPoint(ts_block,i_star,block_size,raw_length,1, j_star,j_star_bit_width)){
          ArrayList<Integer> tmp_tv = ts_block_reorder.get(i_star);
          if(j_star<i_star){
            for(int u=i_star-1;u>=j_star;u--){
              ArrayList<Integer> tmp_tv_cur = new ArrayList<>();
              tmp_tv_cur.add(ts_block_reorder.get(u).get(0));
              tmp_tv_cur.add(ts_block_reorder.get(u).get(1));
              ts_block.set(u+1,tmp_tv_cur);
            }
          }else{
            for(int u=i_star+1;u<=j_star;u++){
              ArrayList<Integer> tmp_tv_cur = new ArrayList<>();
              tmp_tv_cur.add(ts_block_reorder.get(u).get(0));
              tmp_tv_cur.add(ts_block_reorder.get(u).get(1));
              ts_block.set(u-1,tmp_tv_cur);
            }
          }
          ts_block.set(j_star,tmp_tv);
//          raw_bit_width_r = j_star_bit_width;
        }

        ts_block_delta_reorder = getEncodeBitsDelta( ts_block,  block_size,reorder_length,i_star_ready_reorder);
        ArrayList<Byte> cur_encoded_result = encode2Bytes(ts_block_delta_reorder,deviation_list,reorder_length);
        encoded_result.addAll(cur_encoded_result);
      }
    }
    return encoded_result;
  }

  public static int bytes2Integer(ArrayList<Byte> encoded, int start, int num) {
    int value = 0;
    if(num > 4){
      System.out.println("bytes2Integer error");
      return 0;
    }
    for(int i=start;i<num;i++){
      value = value * 2 + encoded.get(i);
    }
    return value;
  }

  public static ArrayList<ArrayList<Integer>> ReorderingDeltaDecoder(ArrayList<Byte> encoded,int td){

    ArrayList<Integer> interval_list = new ArrayList<>();
    ArrayList<Integer> value_list = new ArrayList<>();
    ArrayList<Integer> deviation_list = new ArrayList<>();

    ArrayList<ArrayList<Integer>> data = new ArrayList<>();
    ArrayList<ArrayList<Integer>> ts_block = new ArrayList<>();

    int decode_pos = 0;
    int block_size = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos+=4;
    int r0 = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos+=4;

    int min_delta_interval = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos+=4;
    int min_delta_value = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos+=4;

    int interval0 = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos+=4;
    int value0 = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos+=4;

    int max_bit_width_interval = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos += 4;
    for(int i=0;i<block_size;i++){
      int interval = 0;
      for(int j=0;j<max_bit_width_interval;j++){
        interval = interval * 2 + encoded.get(decode_pos+j);
      }
      interval += min_delta_interval;
      interval_list.add(interval);
      decode_pos += max_bit_width_interval;
    }

    int max_bit_width_value = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos += 4;
    for(int i=0;i<block_size;i++){
      int value = 0;
      for(int j=0;j<max_bit_width_value;j++){
        value = value * 2 + encoded.get(decode_pos+j);
      }
      value += min_delta_value;
      value_list.add(value);
      decode_pos += max_bit_width_value;
    }

    int max_bit_width_deviation = bytes2Integer(encoded,decode_pos,decode_pos+4);
    decode_pos += 4;
    for(int i=0;i<block_size;i++){
      int deviation = 0;
      for(int j=0;j<max_bit_width_deviation;j++){
        deviation = deviation * 2 + encoded.get(decode_pos+j);
      }
      deviation_list.add(deviation);
      decode_pos += max_bit_width_deviation;
    }

    for(int i=0;i<block_size;i++){
      for(int j=0;j<block_size-i-1;j++){
        if(interval_list.get(i)>interval_list.get(i+1)){
          int tmp_interval=interval_list.get(i);
          interval_list.set(i,interval_list.get(i+1));
          interval_list.set(i+1,tmp_interval);
          int tmp_value=value_list.get(i);
          value_list.set(i,value_list.get(i+1));
          value_list.set(i+1,tmp_value);
        }
      }
    }

    int di_pre = interval0;
    int vi_pre = value0;
    for(int i=0;i<block_size;i++){
      int vi = vi_pre + value_list.get(i);
      vi_pre = vi;

      int ri = interval_list.get(i) * td;
      int di = di_pre + deviation_list.get(i);
      di_pre = di;
      int timestampi = ri + di;

      ArrayList<Integer> tmp_datai = new ArrayList<>();
      tmp_datai.add(timestampi);
      tmp_datai.add(vi);
      data.add(tmp_datai);
    }
    return data;
  }


  public static void main(@org.jetbrains.annotations.NotNull String[] args) throws IOException {
//    ArrayList<Integer> test = new ArrayList<>();
//    for(int i=0;i<8;i++){
//      test.add(i);
//    }
//    System.out.println(bitPacking(test,3)[2]);
//    System.out.println((byte)15);

    String inputPath =
            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\Metro-Traffic"; // the direction of input compressed data
//    String Output =
//        "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\Metro-Traffic_ratio.csv"; // the direction of output compression ratio and

//    String inputPath =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\Nifty-Stocks"; // the direction of input compressed data
//    String Output =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\Nifty-Stocks_ratio.csv"; // the direction of output compression ratio and

//    String inputPath =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\USGS-Earthquakes"; // the direction of input compressed data
//    String Output =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\USGS-Earthquakes_ratio.csv"; // the direction of output compression ratio and

//    String inputPath =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\Cyber-Vehicle"; // the direction of input compressed data
//    String Output =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\Cyber-Vehicle_ratio.csv"; // the direction of output compression ratio and

//    String inputPath =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\TH-Climate"; // the direction of input compressed data
//    String Output =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\TH-Climate_ratio.csv"; // the direction of output compression ratio and


//    String inputPath =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\TY-Transport"; // the direction of input compressed data
//    String Output =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\TY-Transport_ratio.csv"; // the direction of output compression ratio and

//        String inputPath =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\iotdb_test\\TY-Fuel"; // the direction of input compressed data
//    String Output =
//            "C:\\Users\\xiaoj\\Documents\\GitHub\\encoding-reorder\\reorder\\result_evaluation\\compression_ratio\\TY-Fuel_ratio.csv"; // the direction of output compression ratio and


    String Output =
            "C:\\Users\\xiaoj\\Desktop\\test_ratio.csv"; // the direction of output compression ratio and

    // speed
    int repeatTime = 1; // set repeat time

    File file = new File(inputPath);
    File[] tempList = file.listFiles();

    CsvWriter writer = new CsvWriter(Output, ',', StandardCharsets.UTF_8);

    String[] head = {
            "Input Direction",
            "Encoding Algorithm",
//      "Compress Algorithm",
            "Encoding Time",
            "Decoding Time",
//      "Compress Time",
//      "Uncompress Time",
            "Compressed Size",
            "Compression Ratio"
    };
    writer.writeRecord(head); // write header to output file

    assert tempList != null;

    for (File f : tempList) {
      InputStream inputStream = Files.newInputStream(f.toPath());
      CsvReader loader = new CsvReader(inputStream, StandardCharsets.UTF_8);
      ArrayList<ArrayList<Integer>> data = new ArrayList<>();

      // add a column to "data"
      loader.readHeaders();
      data.clear();
      while (loader.readRecord()) {
        ArrayList<Integer> tmp = new ArrayList<>();
        tmp.add(Integer.valueOf(loader.getValues()[0]));
        tmp.add(Integer.valueOf(loader.getValues()[1]));
        data.add(tmp);
      }
      inputStream.close();
      long encodeTime = 0;
      long decodeTime = 0;
      double ratio = 0;
      double compressed_size = 0;
      for (int i = 0; i < repeatTime; i++) {
        long s = System.nanoTime();
        ArrayList<Byte> buffer = ReorderingDeltaEncoder(data, 8, 3600);
        long e = System.nanoTime();
        encodeTime += (e - s);
        compressed_size += buffer.size();
        double ratioTmp =
                (double) buffer.size() / (double) (data.size() * Integer.BYTES);
        ratio += ratioTmp;
        s = System.nanoTime();
        ReorderingDeltaDecoder(buffer,3600);
        e = System.nanoTime();
        decodeTime += (e-s);
      }


      ratio /= repeatTime;
      compressed_size /= repeatTime;
      encodeTime /= repeatTime;
      decodeTime /= repeatTime;

      String[] record = {
              f.toString(),
              "Reordering",
              String.valueOf(encodeTime),
              String.valueOf(decodeTime),
              String.valueOf(compressed_size),
              String.valueOf(ratio)
      };
      writer.writeRecord(record);
    }
    writer.close();
  }
}

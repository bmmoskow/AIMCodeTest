package ben.math.distribution.resources.Models;

import com.fasterxml.jackson.annotation.*;

public class HttpHeaders {
    public String server;

    public String date;

    @JsonProperty("content-type")
    public String content_type;

    public String content;

    @JsonProperty("content-length")
    public String content_length;

    public String connection;

    @JsonProperty("x-amzn-requestid")
    public String x_amzn_equestid;

    @JsonProperty("x-amz-crc32")
    public String x_amz_crc32;
}
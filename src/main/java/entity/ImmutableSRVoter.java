package entity;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import java.util.Map;

public final class ImmutableSRVoter {

  private int totalVoter;
  private Long totalVotes;
  private Map<String, Long> voteMap;
  private final String srAddress;
  private final String timestamp;

  public ImmutableSRVoter(String srAddress, Map<String, Long> voteMap, String timestamp) {
    this.srAddress = srAddress;
    this.voteMap = voteMap;
    this.timestamp = timestamp;
    this.totalVoter = makeTotalVoter();
    this.totalVotes = makeTotalVotes();
  }

  public Long getTotalVotes() {
    return this.totalVotes;
  }

  public int getTotalVoter() {
    return this.totalVoter;
  }

  public String getTimestamp(){
    return this.timestamp;
  }

  public int makeTotalVoter() {
    return this.voteMap.size();
  }

  public Long makeTotalVotes() {
    Long sum = 0L;
    for (Map.Entry<String, Long> entry : this.voteMap.entrySet()) {
      sum += entry.getValue();
    }
    return sum;
  }

  public String getSrAddress() {
    return srAddress;
  }

  public Map<String, Long> getVoteMap() {
    return voteMap;
  }

  public String getKey() {
    StringBuilder sb = new StringBuilder();
    sb.append(this.srAddress).append("_").append(String.valueOf(this.totalVotes)).append("_")
        .append(String.valueOf(totalVoter));
    return sb.toString();
  }

  public String toString() {
    SimplePropertyPreFilter filter = new SimplePropertyPreFilter(Object.class, "totalVoter",
        "totalVotes", "timestamp", "srAddress");
    //转成json
    String json = JSON.toJSONString(this, filter);
    return json;
  }
}
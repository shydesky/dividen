package entity;


import java.util.Map;

public final class ImmutableSRVoter {

  private int totalVoter;
  private Long totalVotes;
  private Map<String, Long> voteMap;
  private final String srAddress;

  public ImmutableSRVoter(String srAddress, Map<String, Long> voteMap) {
    this.srAddress = srAddress;
    this.voteMap = voteMap;
    this.totalVoter = makeTotalVoter();
    this.totalVotes = makeTotalVotes();
  }

  public Long getTotalVotes(){
    return this.totalVotes;
  }

  public int getTotalVoter(){
    return this.totalVoter;
  }

  public int makeTotalVoter() {
    return this.voteMap.size();
  }

  public Long makeTotalVotes() {
    Long sum = 0L;
    for(Map.Entry<String, Long> entry: this.voteMap.entrySet()){
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
}
package za.co.ixengineers.legal.matter;
import com.fasterxml.jackson.annotation.JsonCreator;
public final class Domain{
 private Domain(){}
 public enum MatterType{
  DEBT_COLLECTION,LITIGATION_DISPUTE,CONTRACT_REVIEW,
  CONTRACT_DRAFTING,CONTRACT_MANAGEMENT,CRF_CCRF,AD_HOC;
  @JsonCreator public static MatterType parse(String v){
   return valueOf(v.trim().toUpperCase().replace(" / ","_").replace("/","_").replace(" ","_"));
  }
 }
 public enum MatterStatus{
  DRAFT_INTAKE,AWAITING_REQUESTER_INFORMATION,
  SUBMITTED,ASSIGNED,IN_PROGRESS,AWAITING_THIRD_PARTY,
  AWAITING_PM_BUSINESS,PENDING_EXTERNAL_OUTCOME,FINALISED,
  CLOSED,CANCELLED;
  @JsonCreator public static MatterStatus parse(String v){
   return valueOf(v.trim().toUpperCase().replace("/","_").replace(" ","_"));
  }
 }
 public enum Urgency{
  NORMAL,URGENT,EXTREMELY_URGENT;
  @JsonCreator public static Urgency parse(String v){
   return valueOf(v.trim().toUpperCase().replace(" ","_"));
  }
 }
 public enum DocumentCategory{
  INVOICE,SIGNED_AGREEMENT,SUPPORTING_DOCUMENT,
  PLEADING,DEMAND,CONTRACT,SCHEDULE,ANNEXURE,APPENDIX,
  CORRESPONDENCE,CLOSURE_EVIDENCE,LEGAL_OUTPUT,OTHER;
  @JsonCreator
  public static DocumentCategory parse(String v){
   return valueOf(v.trim().toUpperCase().replace(" / ","_").replace("/","_").replace(" ","_"));
  }
 }
}
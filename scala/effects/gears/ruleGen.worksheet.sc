type PPGStatus
type PStatus

trait MissMatchType[PStatus, PPGStatus] {
  def getMissMatchType: Option[MissMatchType[PStatus, PPGStatus]] = None
}

object MissMatchType {
  // non-overlapping miss match types
  case object ActiveLapsing extends MissMatchType
  case object OnholdDifferentOnhold extends MissMatchType
  case object OnholdnonExit extends MissMatchType
  case object NonExistOnHoldExit extends MissMatchType
  case object CancelExist extends MissMatchType
  case object CancelNonExist extends MissMatchType

}

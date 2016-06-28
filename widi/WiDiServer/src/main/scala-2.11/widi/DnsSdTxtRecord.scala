package widi

case class DnsSdTxtRecord(fullDomainName: String, txtRecordMap: Map[String, String], var srcDevice: Device)

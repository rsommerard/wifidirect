import scala.util.Try
import sys.process._

val adbPath = {
  if (Try("/android-sdk-linux/platform-tools/adb version".!).isSuccess)
    "/android-sdk-linux/platform-tools/adb"
  else
    "/home/romain/Android/Sdk/platform-tools/adb"
}
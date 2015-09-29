import java.io.ByteArrayInputStream

import com.sosacorp.filestore.{FilestoreClient, FilestoreClientProperties}
import org.apache.commons.io.IOUtils
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by nickdeyoung on 9/29/15.
 */

class FilestoreClientSpec extends PlaySpec with OneAppPerSuite {

  // Override app if you need a FakeApplication with other than
  // default parameters.
  implicit override lazy val app: FakeApplication = FakeApplication(

  )

  "a byte array" must {
    "read and write" in {
      running(FakeApplication()) {
        val client = new FilestoreClient with FilestoreClientProperties
        val stream = new ByteArrayInputStream("Helloworld".getBytes)

        val bytesWritten = Await.result(client.write(stream, "/foo"), Duration.Inf)

        bytesWritten match {
          case Left(msg) =>
            fail(msg)
          case Right(bytes) =>
            println(s"wrote ${"Helloworld".getBytes.size} bytes")
            assert(bytes == "Helloworld".getBytes.size)
        }

        val bytesRead = Await.result(client.read("/foo"), Duration.Inf)

        bytesRead match {
          case Left(msg) =>
            fail(msg)
          case Right(bytes) =>
            val ba = IOUtils.toByteArray(bytes).size
            println(s"read ${ba} bytes")
            assert(ba == "Helloworld".getBytes.size)
        }
      }
    }
  }
}

import java.io.ByteArrayInputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

import akka.dispatch.Futures
import com.google.common.base.Stopwatch
import com.sosacorp.filestore.{FilestoreClient, FilestoreClientProperties}
import org.apache.commons.io.IOUtils
import org.scalatestplus.play.{OneAppPerSuite, PlaySpec}
import play.api.test.FakeApplication
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, _}
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


        val is = getClass.getResourceAsStream("/testImage.jpg")
        val ba = Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte).toArray

        val bytesWritten = Await.result(client.write(new ByteArrayInputStream(ba), "/foo"), Duration.Inf)

        bytesWritten match {
          case Left(msg) =>
            fail(msg)
          case Right(bytes) =>
            println(s"wrote ${ba.size} bytes")

        }

        val bytesRead = Await.result(client.read("/foo"), Duration.Inf)

        bytesRead match {
          case Left(msg) =>
            fail(msg)
          case Right(bytes) =>
            val ba = IOUtils.toByteArray(bytes).size
            println(s"read ${ba} bytes")

        }
      }
    }
  }

  "a byte array" must {
    "read and write stress" in {
      running(FakeApplication()) {
        import scala.concurrent.ExecutionContext.Implicits.global

        val client = new FilestoreClient with FilestoreClientProperties

        val futures = for (i <- 0 to 1000) yield {
          Future {
//            println("starting write future")
            val is = getClass.getResourceAsStream("/testImage.jpg")
            val ba = Stream.continually(is.read).takeWhile(_ != -1).map(_.toByte).toArray
            val path = "/bar/" + UUID.randomUUID().toString + "/foo"
            val rtn = Await.result(client.write(new ByteArrayInputStream(ba), path ), Duration.Inf)
//            println("finish write future" + rtn)
          }
        }

        val all = Future sequence futures

        val sw = Stopwatch.createStarted()
        Await.result(all, Duration.Inf)
        sw.stop()
        println("uploads took " + sw.elapsed(TimeUnit.MILLISECONDS) + " ms")

      }


    }
  }
}

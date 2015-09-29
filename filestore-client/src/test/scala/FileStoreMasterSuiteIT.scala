import org.junit.runner.RunWith
import org.scalatest.{BeforeAndAfterAll, Suites}
import org.scalatest.junit.JUnitRunner

/**
 * Created by nickdeyoung on 9/29/15.
 */
@RunWith(classOf[JUnitRunner])
class FileStoreMasterSuiteIT extends Suites(
  new FilestoreClientSpec
) with BeforeAndAfterAll {

}

import org.scalatestplus.play._
import services.MapURLShortener
import java.net.URL

class URLShortenerSpec extends PlaySpec {

  "MapURLShortener" must {
    "return the same shorten url when request url's hosts are the same" in {
      val mapURLShortener = new MapURLShortener()
      
      val hostString = "en.wikipedia.org"
      val urlFunc = host => s"https://$host/wiki/URL_shortening"
      mapURLShortener.addUrl(new URL(urlFunc(hostString))) must be(
        mapURLShortener.addUrl(new URL(urlFunc(hostString))))
      mapURLShortener.getAll().length must be(1)

      mapURLShortener.addUrl(new URL(urlFunc(hostString))) must be(
        mapURLShortener.addUrl(new URL(urlFunc(hostString.toUpperCase))))
      mapURLShortener.getAll().length must be(1)
    }

    "return the same shorten url when request url's protocols are the same" in {
      val mapURLShortener = new MapURLShortener()
      
      val urlFunc = protocol =>  s"$protocol://en.wikipedia.org/wiki/URL_shortening"
      
      mapURLShortener.addUrl(new URL(urlFunc("https"))) must be(
        mapURLShortener.addUrl(new URL(urlFunc("HTTPS"))))
      
      mapURLShortener.getAll().length must be(1)
    }
    
    "return different shorten url when request url's pathes are different" in {
      val mapURLShortener = new MapURLShortener()

      val urlFunc = path => s"https://en.wikipedia.org/$path"
      val pathString = "wiki/URL_shortening"
      mapURLShortener.addUrl(new URL(urlFunc(pathString))) must not be(
        mapURLShortener.addUrl(new URL(urlFunc(pathString+"12"))))
      mapURLShortener.getAll().length must be(2)

      mapURLShortener.addUrl(new URL(urlFunc(pathString))) must not be(
        mapURLShortener.addUrl(new URL(urlFunc(pathString.toUpperCase))))
      mapURLShortener.getAll().length must be(3)
    }


    "work correctly in concurrent environment" in {
      import scala.concurrent._
      import ExecutionContext.Implicits.global
      import scala.concurrent.duration._

      val mapURLShortener = new MapURLShortener()

      val urlFunc = path => s"https://en.wikipedia.org/$path"
      val urlCount = 10000
      val urls = for(i <- 0 until urlCount) yield urlFunc(i.toString)

      val futures = for(i <- 0 until 4) yield Future {
        val shuffledUrls = scala.util.Random.shuffle(urls)
        shuffledUrls.map(url => mapURLShortener.addUrl(new URL(url)))
      }
      for(f <- futures) Await.ready(f, Duration.Inf)
      mapURLShortener.getAll().length must be(urlCount)
    }
  }
}
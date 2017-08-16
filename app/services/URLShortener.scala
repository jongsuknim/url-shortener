package services

import javax.inject._
import java.net.URL


trait URLShortener {
	def addUrl(urlString: URL): String
	def getUrl(shorten: String): Option[URL]
	def getAll(): Iterator[(String, URL)]
}

@Singleton
class MapURLShortener extends URLShortener {
	import scala.collection._	
	import java.util.concurrent.ConcurrentHashMap
	import scala.collection.JavaConverters._

	def addUrl(url: URL): String = {
		urls.synchronized {
			addUrlInternal(url, 0)
		}
	}

	def getUrl(shorten: String): Option[URL] = {
		urls.get(shorten)
	}

	def getAll(): Iterator[(String, URL)] = {
		urls.iterator
	}

	private val urls: concurrent.Map[String, URL] = new ConcurrentHashMap().asScala

	private def addUrlInternal(url: URL, seq: Int): String = {
		val lowercaseURL = new URL(url.getProtocol, url.getHost.toLowerCase, url.getPort, url.getFile)
          
		val shorten = makeShorten(lowercaseURL, seq)				
		urls.get(shorten) match {
			case None => urls.put(shorten, lowercaseURL); shorten
			case Some(value_url) if lowercaseURL == value_url => shorten
			case _ => addUrlInternal(lowercaseURL, seq+1)
		}
	}

	private val md = java.security.MessageDigest.getInstance("SHA-1")
	private def hash(value: String): String = {
		import java.util.Base64
		Base64.getUrlEncoder().encodeToString(
			md.digest(value.getBytes)).substring(0,8)
	}

	private def makeShorten(url: URL, seq: Int = 0): String = {
		hash(url.toString + ";" + seq.toString)
	}
}








package services

import javax.inject._
import java.net.URL


trait URLShortener {
	def addUrl(urlString: URL): String
	def getURL(shorten: String): Option[URL]
	def getAll(): Iterator[(String, URL)]
}

@Singleton
class MapURLShortener extends URLShortener {
	import scala.collection._	
	import java.util.concurrent.ConcurrentHashMap
	import scala.collection.JavaConverters._

	val urls: concurrent.Map[String, URL] = new ConcurrentHashMap().asScala

	def addUrlInternal(url: URL, seq: Int): String = {
		val shorten = makeShorten(url, seq)				
		urls.get(shorten) match {
			case None => urls.put(shorten, url); shorten
			case Some(value_url) if url == value_url => shorten
			case _ => addUrlInternal(url, seq+1)
		}
	}

	def getAll(): Iterator[(String, URL)] = {
		urls.iterator
	}

	override def addUrl(url: URL): String = {
		urls.synchronized {
			addUrlInternal(url, 0)
		}
	}

	override def getURL(shorten: String): Option[URL] = {
		urls.get(shorten)
	}

	val md = java.security.MessageDigest.getInstance("SHA-1")
	def hash(value: String): String = {
		import java.util.Base64
		Base64.getUrlEncoder().encodeToString(
			md.digest(value.getBytes)).substring(0,8)
	}

	def makeShorten(url: URL, seq: Int = 0): String = {
		hash(url.toString + hash(seq.toString))
	}
}








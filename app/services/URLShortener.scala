package services

import javax.inject._

case class URLData(url: String)

trait URLShortener {
	def addUrl(url: URLData): String
	def getURL(shorten: String): Option[URLData]
	def getAll(): Iterator[(String, URLData)]
}

@Singleton
class MapURLShortener extends URLShortener {
	import scala.collection._	
	import java.util.concurrent.ConcurrentHashMap
	import scala.collection.JavaConverters._

	val urls: concurrent.Map[String, URLData] = new ConcurrentHashMap().asScala

	def addUrlInternal(url: URLData, seq: Int): String = {
		val shorten = makeShorten(url, seq)				
		urls.get(shorten) match {
			case None => urls.put(shorten, url); shorten
			case Some(value_url) if url == value_url => shorten
			case _ => addUrlInternal(url, seq+1)
		}
	}

	def getAll(): Iterator[(String, URLData)] = {
		urls.iterator
	}

	override def addUrl(url: URLData): String = {
		urls.synchronized {
			addUrlInternal(url, 0)
		}
	}

	override def getURL(shorten: String): Option[URLData] = {
		urls.get(shorten)
	}

	val md = java.security.MessageDigest.getInstance("SHA-1")
	def hash(value: String): String = {
		import java.util.Base64
		Base64.getUrlEncoder().encodeToString(
			md.digest(value.getBytes)).substring(0,8)
	}

	def makeShorten(url: URLData, seq: Int = 0): String = {
		"xxxx_" + seq.toString
	}

	def makeShorten2(url: URLData, seq: Int = 0): String = {
		hash(url.url + hash(seq.toString))
	}
}








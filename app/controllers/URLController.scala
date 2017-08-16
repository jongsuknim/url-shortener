package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Flash

import play.api.data.Form
import play.api.data.Forms.{ mapping, single, nonEmptyText }

import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.i18n.Messages

import services.URLShortener
import java.net.URL

@Singleton
class URLController @Inject() (urlShortener: URLShortener, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  private val urlForm: Form[String] = Form(
  	single(
    	"url" -> nonEmptyText
  	)
	)
	
  def index = Action { implicit request =>
    val form = 
      if (request2flash.get("formError").isDefined)
        urlForm.bind(request2flash.data)
      else
        urlForm
        
    Ok(views.html.url.index(form, urlShortener.getAll(), request.host))
  } 

  def create = Action { implicit request =>
    val newUrlForm = urlForm.bindFromRequest()

    newUrlForm.fold(
      hasErrors = { form =>
        Redirect(routes.URLController.index()).flashing(
          Flash(form.data) + 
          ("formError" -> "등록에러"))
      },
      success = { urlString =>
        try {
          val url = new URL(urlString)
          url.toURI() // for checking url validation
          val shorten = urlShortener.addUrl(url)
          Redirect(routes.URLController.index()).flashing(
            "success" -> s"등록 성공: ${url.toString} => ${request.host}/$shorten")
        } catch {
          case e : Throwable =>        
            Redirect(routes.URLController.index()).flashing(
              ("error" -> e.getMessage()))
        }
      })
  }

  def go(shortenUrl: String) = Action { implicit request =>
    urlShortener.getUrl(shortenUrl) match {
      case None => NotFound(s"${request.host}/$shortenUrl 페이지가 존재하지 않습니다.")
      case Some(url) => Redirect(url.toString)
    }
  }
}

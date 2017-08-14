package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Flash

import play.api.data.Form
import play.api.data.Forms.{ mapping, single, nonEmptyText }

import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.i18n.Messages

import services.{ URLShortener, URLData }

@Singleton
class URLController @Inject() (urlShortener: URLShortener, val messagesApi: MessagesApi) extends Controller with I18nSupport {

  private val urlForm: Form[URLData] = Form(
  	mapping(
    	"url" -> nonEmptyText
  	)(URLData.apply)(URLData.unapply)
	)

	
  def index = Action { implicit request =>
  	Ok(views.html.url.index(urlForm, urlShortener.getAll(), request.host))
  } 

  def create = Action { implicit request =>
    val newUrlForm = urlForm.bindFromRequest()
    newUrlForm.fold(
      hasErrors = { form =>
        Redirect(routes.URLController.index()).flashing(
          Flash(form.data) + ("error" -> s"등록 에러"))
      },
      success = { newUrl =>
        val shorten = urlShortener.addUrl(newUrl)
        Redirect(routes.URLController.index()).flashing(
          "success" -> s"등록 성공")
      })
  }

  def go(shortenUrl: String) = Action { implicit request =>
    urlShortener.getURL(shortenUrl) match {
      case None => NotFound(s"${request.host}/$shortenUrl 페이지가 존재하지 않습니다.")
      case Some(url) => Redirect(url.url)
    }
  }
}

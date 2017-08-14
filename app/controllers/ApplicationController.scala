package controllers

import play.api._
import play.api.mvc._


class ApplicationController extends Controller {
  def index = Action {
    Redirect(routes.URLController.index())
  }
}

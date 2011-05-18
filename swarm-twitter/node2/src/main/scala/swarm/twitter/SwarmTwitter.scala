package swarm.twitter

import javax.servlet.http.{HttpServlet, HttpServletRequest => HSReq, HttpServletResponse => HSResp}

class SwarmTwitter extends HttpServlet {

  val swarmBridge = new SwarmBridge(9998, 9997)

  override def doGet(req: HSReq, resp: HSResp) = {

    val x = req.getParameter("x")
    if (x != null) {
      swarmBridge.updateX(x)
      resp.sendRedirect("/")
    }
    resp.getWriter().print(<html>
      <head>
        <title>SwarmTwitter</title>
      </head>
      <body>
        <h1>SwarmTwitter</h1>
        <div>X:
          {swarmBridge.getX(java.util.UUID.randomUUID.toString)}
        </div>
      </body>
    </html>
    )
  }
}
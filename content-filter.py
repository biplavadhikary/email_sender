from aiosmtpd.controller import Controller
from smtplib import SMTP as Client
from random import randint


class CustomSMTPHandler:
	async def handle_DATA(self, _server, _session, envelope):
		body = envelope.content.decode("utf8", errors="replace")
		try:
			for recpt in envelope.rcpt_tos:
				print(recpt)
				client = Client("localhost", 10026)
				data = body.replace("<img src='http://www.programacion.net/files/article/20160124010121_url1.jpg'>", "<img id=" + str(randint(0, 22)) + " src='http://www.programacion.net/files/article/20160124010121_url1.jpg'>")
				client.sendmail(envelope.mail_from, recpt, data)
				client.quit()
		except Exception as e:
			print(e)
		
		return "250 OK"


server = Controller(CustomSMTPHandler(), "127.0.0.1", 10025)
server.start()
input("Server started. Press Return to quit.")
server.stop()

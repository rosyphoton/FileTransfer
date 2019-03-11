from flask import Flask
from flask import render_template
from flask import send_file

app = Flask(__name__, 	static_url_path='/templates/radios')

@app.route("/", methods=['GET'])
def index():
    return render_template('index.html')

@app.route("/sounds/test", methods=['GET'])
def sounds_Test():
	path_to_file = "./sounds/test.wav"
	return send_file(
		path_to_file, 
        mimetype="audio/wav", 
        as_attachment=True, 
        attachment_filename="test.wav")

if __name__ == "__main__":
	app.run()
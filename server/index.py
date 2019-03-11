from generate_audio import gen_fsk_audio, gen_motion_sensor_audio

from flask import Flask, render_template, send_file, request
from werkzeug.utils import secure_filename

import os

app = Flask(__name__)
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN']=True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS']=True
app.config['UPLOAD_FOLDER'] = 'static\\uploads'
app.config['ALLOWED_EXTENSIONS'] = set(['png', 'txt', 'jpg', 'wav'])

def allowed_file(filename):
    return '.' in filename and \
            filename.rsplit('.', 1)[1] in app.config['ALLOWED_EXTENSIONS']

@app.route("/upload", methods=['GET','POST'])
def upload():
    upload_file = request.files['file']
    file_dir = os.path.join(app.root_path, app.config['UPLOAD_FOLDER'])
    if not os.path.exists(file_dir):
        os.makedirs(file_dir)

    if upload_file and allowed_file(upload_file.filename):
        filename = secure_filename(upload_file.filename)
        upload_file.save(os.path.join(file_dir, filename))
        return 'hello, successful'
    else:
        return 'hello, failed'

@app.route("/test", methods=['GET'])
def index_test():
    return render_template('index_simple.html')

@app.route("/sounds/test", methods=['GET'])
def sounds_Test():
	path_to_file = "./sounds/simpleTestSound.wav"
	return send_file(
		path_to_file, 
        mimetype="audio/wav", 
        as_attachment=True, 
        attachment_filename="test.wav")


@app.route("/", methods=['GET', 'POST'])
def index():
    return render_template('index_background.html')


@app.route("/sounds/get", methods=['GET'])
def getSoundFile():
    file = request.args.get('file')
    path_to_file = "./sounds/" + file + ".wav"
    return send_file(
        path_to_file,
        mimetype="audio/wav",
        as_attachment=True,
        attachment_filename=file + ".wav")


@app.route("/sendText", methods=['GET'])
def sendText():
    data = request.args.get('data')
    if not (os.path.isfile("./sounds/" + data + ".wav")):
        gen_fsk_audio(data, "./sounds/")
    return "/sounds/get?file=" + data


@app.route("/sendVibration", methods=['GET'])
def sendVibration():
    data = request.args.get('data')
    if not (os.path.isfile("./sounds/" + data + "_vib.wav")):
        gen_motion_sensor_audio(data, "./sounds/")
    return "/sounds/get?file=" + data + "_vib"

@app.route("/register", methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username = request.form['username']
        password = request.form['password']

        print('username:'+username)
        print('password:'+password)
        return 'register successfully'


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=80, debug=True)

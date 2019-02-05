#-*-coding:utf-8-*-
from flask import Flask
from flask import request, make_response, redirect
from werkzeug.utils import secure_filename
import os

app = Flask(__name__)
basedir=os.path.abspath(os.path.dirname(__file__))
app.config['SQLALCHEMY_COMMIT_ON_TEARDOWN']=True
app.config['SQLALCHEMY_TRACK_MODIFICATIONS']=True
app.config['UPLOAD_FOLDER'] = 'statics\\uploads'
app.config['ALLOWED_EXTENSIONS'] = set(['png', 'txt', 'jpg'])

def allowed_file(filename):
    return '.' in filename and \
            filename.rsplit('.', 1)[1] in app.config['ALLOWED_EXTENSIONS']

@app.route('/')
def test():
    return '服务器正常运行'

@app.route('/upload', methods=['POST'])
def upload():
    upload_file = request.files['file']
    file_dir = os.path.join(app.root_path, app.config['UPLOAD_FOLDER'])
    if not os.path.exists(file_dir):
        os.makedirs(file_dir)

    if upload_file and allowed_file(upload_file.filename):
        filename = secure_filename(upload_file.filename)
        upload_file.save(os.path.join(file_dir, filename))
        return 'hello,successful'
    else:
        return 'hello,failed'

@app.route('/show/<string:filename>', methods=['GET'])
def show_photo(filename):
    file_dir = os.path.join(basedir, app.config['UPLOAD_FOLDER'])
    # filename = "111.jpg"
    if request.method == 'GET':
        if filename is None:
            pass
        else:
            image_data = open(os.path.join(file_dir, '%s' % filename), "rb").read()
            response = make_response(image_data)
            response.headers['Content-Type'] = 'image/jpg'
            return response
    else:
        pass

#此方法处理用户注册
@app.route('/register',methods=['GET', 'POST'])
def register():
    if request.method == 'POST':
        username=request.form['username']
        password=request.form['password']

        print('username:'+username)
        print('password:'+password)
        return 'register successfully'

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=80, debug=True)


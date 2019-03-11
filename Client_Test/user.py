import requests

# user_info = {'username': 'Ahri', 'password': '123123'}
# r = requests.post("http://192.168.1.13/register", data=user_info)
files = {'file': open('abc.txt', 'rb')}
user_info = {'username': 'Akari'}
r = requests.post("http://172.18.88.79/upload", data=user_info, files=files)

print r.text
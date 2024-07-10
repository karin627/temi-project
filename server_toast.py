# from win10toast import ToastNotifier
from win11toast import toast
import socket
import keyboard


def show_notification(response):
    toast(response)
    
def handle_client(client_socket):
    try:
        while True:
            data = client_socket.recv(1024).decode('utf-8')
            if not data:
                break  # No more data, connection closed by client

            print(f"Received message from client: {data}")
            
            response = f"Server received: {data}"
            show_notification(response)
            client_socket.send(response.encode('utf-8'))

            if data.lower() == 'bye':
                print("Closing the connection.")
                break
    finally:
        client_socket.close()
                  
def start_server():
      # Server IP address and port
    
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_address = ('192.168.50.169', 5000)
    server_socket.bind(server_address)
    server_socket.listen(1)  # Listen for incoming connections

    print(f"Server is listening on {server_address[0]}:{server_address[1]}")

    while True:
        print("Waiting for a connection...")
        
        client_socket, client_address = server_socket.accept()
        
        print(f"Accepted connection from {client_address}")

        handle_client(client_socket)
        
        
        

        
        
if __name__ == "__main__":
    
    start_server()

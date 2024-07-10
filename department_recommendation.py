import socket
import openai
import os
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import  RecursiveCharacterTextSplitter
from langchain.embeddings.openai import OpenAIEmbeddings
from langchain.vectorstores.chroma import Chroma
from langchain_community.chat_models import ChatOpenAI
from langchain.chains import ConversationalRetrievalChain  
import re    
     
# rag preparation
os.environ["OPENAI_API_KEY"] = "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"
openai.api_key = os.getenv("OPENAI_API_KEY")

# load data
file_path = "D:\project\second\症狀科別對應資料\常見疾病症狀就醫參考2.pdf"
loader = file_path.endswith(".pdf") and PyPDFLoader(file_path)

# split data
splitter = RecursiveCharacterTextSplitter(
    separators = "。",
	chunk_size = 500,
	chunk_overlap = 0,
)
texts = loader.load_and_split(splitter)

# embedding
embeddings = OpenAIEmbeddings()

# change vectors into database
vectorstore = Chroma.from_documents(texts, embeddings)

# conversation
qa = ConversationalRetrievalChain.from_llm(
    ChatOpenAI(temperature=0),
    vectorstore.as_retriever()
)
    
# start_server()
server_address = ('192.168.50.169', 5000)  # Server IP address and port
server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    
server_socket.bind(server_address)
server_socket.listen(1)  # Listen for incoming connections

print(f"Server is listening on {server_address[0]}:{server_address[1]}")

while True:
    print("Waiting for a connection...")
        
    client_socket, client_address = server_socket.accept()
        
    print(f"Accepted connection from {client_address}")
    try:
        while True:
            data = client_socket.recv(1024).decode('utf-8')
            if not data:
                break  # No more data, connection closed by client

            print(f"Received message from client: {data}")
            
            response = data
            
            eighteen = True

            symptom = response
            query = "根據下列症狀描述，請列出患者可以掛號的科別，並遵守以下準則: 1.用頓號隔開每一個科別 2.請不要用到句號 3.不要用句子回答 4.用繁體中文回答 "
            if eighteen:
                query  = query + "患者為十八歲以上 "
            else:
                query  = query + "患者為十八歲以下 "
            query = query + symptom
            result = qa({"question": query, "chat_history": []})
            answer = result["answer"]
            print(answer)
            answer_list = re.split('[,#;!? ]', answer)
            answer = answer_list[0]
            answer_list.pop(0)
            for item in answer_list:
                answer = answer + "、" + item
            answer = answer + '\n'
            print("Answer:", answer)
            client_socket.send(answer.encode('utf-8'))
            print("send to client : " , answer)

            if data.lower() == 'bye':
                print("Closing the connection.")
                break
    finally:
        client_socket.close()

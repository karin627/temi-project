import socket
import openai
import os
from langchain_community.document_loaders import PyPDFLoader
from langchain.text_splitter import  RecursiveCharacterTextSplitter
from langchain_openai import OpenAIEmbeddings
from langchain_community.vectorstores import Chroma
from langchain.chains import ConversationalRetrievalChain  
from llama_index.postprocessor.flag_embedding_reranker import FlagEmbeddingReranker
from langchain_openai import ChatOpenAI
from langchain.schema import HumanMessage
import re    
import pandas as pd
import ast

# rag preparation
os.environ["OPENAI_API_KEY"] = "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"
openai.api_key = os.getenv("OPENAI_API_KEY")

# department list
department_list = ["一般內科","腸胃肝膽科","腎臟內科" ,"心臟血管內科", "過敏免疫風濕科", "感染科", "胸腔內科", "神經內科", "新陳代謝科", "血液腫瘤科",
                   "一般外科", "心臟血管外科", "神經外科", "胸腔外科", "骨科", "整型外科", "大腸直腸外科", "泌尿外科", "脊椎外科", 
                   "家庭醫學科", "老人醫學科", "小兒科", "小兒心臟科", "婦產科", "眼科", "耳鼻喉科", "皮膚科", "復健科", "美容特別門診", "兒童青少年科", "身心內科", "生理回饋特診", "疼痛門診",
                   "核子醫學科", "放射治療科", "家庭牙科", "兒童牙科", "牙周病科", "人工植牙科", "中醫科"]

# load data
file_path = "振興看診科別.pdf"
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


llm = ChatOpenAI(temperature=0)


def llm_relevance_score(symptom, department):
    prompt = f"請根據以下症狀描述給出1到5的分數（5為最相關，1為最不相關），只要給分數不要回答原因。症狀描述：{symptom}文本：{department}"
    try:
        response = llm.invoke([HumanMessage(content=prompt)])
        score = float(response.content.split("。")[0].strip())
        return score
    
    except ValueError as e:
        return 1.0  
    
def llm_reselect_valid_department(department):
    prompt_reselect = f"請根據科別名單選出一個與輸入科別最相似的科別，只要返回科別名稱不要回答句子。科別名單:{department_list}輸入科別:{department}"
    try:
        reselect_dept = llm.invoke([HumanMessage(content=prompt_reselect)])
        return reselect_dept.content
    
    except ValueError as e:
        print("重選錯誤")
        return 0

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
            
            symptom = response
            query = "根據下列症狀描述，請列出患者可以掛號的科別，並遵守以下準則: 1.用頓號隔開每一個科別 2.請不要用到句號 3.不要用句子回答 4.用繁體中文回答 "
            
            eighteen = True
            age_clause = "患者為十八歲以上 " if eighteen else "患者為十八歲以下 "
            full_query = query + symptom + age_clause
            
            departments = []
            updated_departments = []
            
            try:
                result = qa.invoke({"question": full_query, "chat_history": []})
                answer = result["answer"].replace("\n"," ")
                departments = list(set(re.split('[、;；，,]', answer.strip("。 ")))) #避免重複科別
            
                if len(departments) > 4:
                    print(f"Too many departments({len(departments)})")
                    departments = departments[:4]
            
                for dept in departments:
                    dept = dept.strip(" ")
                    if dept in department_list:
                        score = llm_relevance_score(symptom,dept)
                        updated_departments.append(dept)
                    else:
                        score = 0.0
                        reselect_dept = llm_reselect_valid_department(dept)
                        print(f"invalid_dept:{dept}, reselect_dept:{reselect_dept}")
                        updated_departments.append(reselect_dept)

                updated_departments = list(set(updated_departments))        
                        
                answer = updated_departments[0]
                updated_departments.pop(0)
                for item in updated_departments:
                    answer = answer + "、" + item
                answer = answer + '\n'
                print("Answer:", answer)
                client_socket.send(answer.encode('utf-8'))
                print("send to client : " , answer)
            
            except Exception as e:
                answer = "Error processing query"
            
            if data.lower() == 'bye':
                print("Closing the connection.")
                break
    finally:
        client_socket.close()

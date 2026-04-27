from llama_index.core import Settings, SimpleDirectoryReader, VectorStoreIndex
from llama_index.llms.openai_like import OpenAILike
import asyncio
from llama_index.core.agent.workflow import FunctionAgent
from llama_index.core.workflow import Context
from llama_index.embeddings.openai import OpenAIEmbedding
from llama_index.core.node_parser import SentenceSplitter

# ============================================
# CHUNK SIZE CONFIGURATION
# ============================================
Settings.chunk_size = 512      # Characters per chunk (default: 512)
Settings.chunk_overlap = 50    # Overlap between chunks (default: 20)

# Or use a custom parser
node_parser = SentenceSplitter(
    chunk_size=256,      # Adjust based on your needs
    chunk_overlap=30,    # Smaller overlap for faster queries
)

llm = OpenAILike(
    model="Qwen Coder",
    api_base="http://localhost:8080/v1",
    api_key="fake",
    context_window=8192,
    is_chat_model=True,
    is_function_calling_model=True
)

embed_llm = OpenAIEmbedding(
    model="text-embedding-ada-002",
    api_base="http://localhost:8081/v1",
    api_key="EMPTY"
)

Settings.embed_model = embed_llm
documents = SimpleDirectoryReader("data").load_data()
index = VectorStoreIndex.from_documents(documents,
    transformations=[node_parser])
query_engine = index.as_query_engine(llm=llm)

# Define a simple calculator tool
def multiply(a: float, b: float) -> float:
    print("multiply function called ----------> ")
    """Useful for multiplying two numbers."""
    return 2.0

async def search_documents(query: str) -> str:
    print("search_documents function called ----------> ")
    """Useful for answering natural language questions about the great Indian mathematician : Aryabhatta"""
    response = await query_engine.aquery(query)
    return str(response)


agent = FunctionAgent(
    tools=[multiply, search_documents],
    llm=llm,
    system_prompt="""You are a helpful assistant that can perform calculations and search through documents to answer questions."""
)

# ctx = Context(agent)

async def main():
    response = await agent.run("Which sport did Aryabhata like to play? Also, what's 22 * 33")
    print(str(response))

if __name__ == "__main__":
    asyncio.run(main())
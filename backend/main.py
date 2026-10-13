from fastapi import FastAPI

app = FastAPI(
    title="ReTech API",
    description="Backend for the ReTech recycling application",
    version="1.0.0"
)

@app.get("/")
def home():
    return {
        "project": "ReTech",
        "message": "ReTech backend is running!"
    }

@app.get("/health")
def health_check():
    return {"status": "healthy"}
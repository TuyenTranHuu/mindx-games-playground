const questions = [
    {
        question: "HTML là viết tắt của từ gì?",
        options: ["Hyper Text Markup Language", "Home Tool Markup Language", "Hyperlinks and Text Markup Language", "Hyper Text Markdown Language"],
        answer: 0
    },
    {
        question: "Thẻ nào dùng để chèn hình ảnh vào trang web?",
        options: ["<img>", "<image>", "<src>", "<picture>"],
        answer: 0
    },
    {
        question: "CSS viết tắt của từ gì?",
        options: ["Creative Style Sheet", "Cascading Style Sheets", "Colorful Style Sheets", "Computer Style Sheets"],
        answer: 1
    },
    {
        question: "Thẻ nào dùng để tạo liên kết trong HTML?",
        options: ["<a>", "<link>", "<href>", "<url>"],
        answer: 0
    },
    {
        question: "Thuộc tính nào dùng để đổi màu chữ trong CSS?",
        options: ["font-color", "text-color", "color", "font-style"],
        answer: 2
    },
    {
        question: "Cách viết comment trong HTML là gì?",
        options: ["// comment", "# comment", "/* comment */", "<!-- comment -->"],
        answer: 3
    },
    {
        question: "Trong JavaScript, kiểu dữ liệu nào sau đây là kiểu số?",
        options: ["'42'", "true", "42", "null"],
        answer: 2
    },
    {
        question: "Hàm nào dùng để in ra console trong JavaScript?",
        options: ["print()", "log()", "console.log()", "echo()"],
        answer: 2
    },
    {
        question: "Câu lệnh nào dùng để khai báo biến trong JavaScript?",
        options: ["int", "var", "define", "value"],
        answer: 1
    },
    {
        question: "Sự kiện nào xảy ra khi người dùng nhấp chuột vào phần tử?",
        options: ["onhover", "onload", "onclick", "onchange"],
        answer: 2
    }
];

let currentQuestionIndex = 0;
let score = 0;
let hasAnswered = false;

const quizContent = document.getElementById("quiz-content");
const questionNumber = document.getElementById("question-number");
const scoreText = document.getElementById("score");
const questionText = document.getElementById("question-text");
const answerList = document.getElementById("answer-list");
const nextButton = document.getElementById("next-button");

function renderQuestion() {
    const currentQuestion = questions[currentQuestionIndex];

    hasAnswered = false;
    questionNumber.textContent = `Câu hỏi ${currentQuestionIndex + 1}/${questions.length}`;
    scoreText.textContent = `Điểm: ${score}`;
    questionText.textContent = currentQuestion.question;
    answerList.innerHTML = "";
    nextButton.disabled = true;

    if (currentQuestionIndex === questions.length - 1) {
        nextButton.textContent = "Kết thúc";
    } else {
        nextButton.textContent = "Câu tiếp theo";
    }

    for (let i = 0; i < currentQuestion.options.length; i++) {
        const answerButton = document.createElement("button");
        answerButton.type = "button";
        answerButton.className = "answer-button";
        answerButton.textContent = currentQuestion.options[i];

        answerButton.addEventListener("click", function () {
            selectAnswer(i);
        });

        answerList.appendChild(answerButton);
    }
}

function selectAnswer(selectedAnswerIndex) {
    if (hasAnswered) {
        return;
    }

    hasAnswered = true;
    const currentQuestion = questions[currentQuestionIndex];
    const answerButtons = document.querySelectorAll(".answer-button");

    if (selectedAnswerIndex === currentQuestion.answer) {
        answerButtons[selectedAnswerIndex].classList.add("correct");
        score++;
        scoreText.textContent = `Điểm: ${score}`;
    } else {
        answerButtons[selectedAnswerIndex].classList.add("wrong");
        answerButtons[currentQuestion.answer].classList.add("correct");
    }

    for (let i = 0; i < answerButtons.length; i++) {
        answerButtons[i].disabled = true;
    }

    nextButton.disabled = false;
}

function showResult() {
    quizContent.innerHTML = `
        <div class="result-screen">
            <h2>Hoàn thành bài Quiz!</h2>
            <p>Điểm của bạn: <strong>${score}/${questions.length}</strong></p>
            <button class="restart-button" id="restart-button" type="button">Làm lại</button>
        </div>
    `;

    const restartButton = document.getElementById("restart-button");
    restartButton.addEventListener("click", restartQuiz);
}

function restartQuiz() {
    currentQuestionIndex = 0;
    score = 0;
    hasAnswered = false;

    window.location.reload();
}

nextButton.addEventListener("click", function () {
    if (!hasAnswered) {
        return;
    }

    if (currentQuestionIndex < questions.length - 1) {
        currentQuestionIndex++;
        renderQuestion();
    } else {
        showResult();
    }
});

renderQuestion();

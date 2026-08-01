/* Shared quiz widget — used by all lesson files */

function initQuiz(containerId, correctIndex, explanation) {
  const container = document.getElementById(containerId);
  if (!container) return;

  const buttons = container.querySelectorAll('.quiz-options button');
  const feedback = container.querySelector('.quiz-feedback');

  buttons.forEach((btn, i) => {
    btn.addEventListener('click', () => {
      buttons.forEach(b => b.disabled = true);
      const isCorrect = i === correctIndex;
      btn.classList.add(isCorrect ? 'correct' : 'incorrect');
      if (!isCorrect) buttons[correctIndex].classList.add('correct');
      feedback.textContent = explanation;
      feedback.className = 'quiz-feedback show ' + (isCorrect ? 'correct' : 'incorrect');
    });
  });
}

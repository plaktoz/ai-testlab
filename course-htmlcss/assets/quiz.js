/**
 * Reusable quiz widget for kids HTML lessons.
 *
 * Usage: add data-quiz sections to the page.
 *
 * <div class="quiz" data-correct="1">
 *   <p class="quiz-question">Which tag is the biggest?</p>
 *   <button class="quiz-btn" data-choice="1">&lt;h1&gt;</button>
 *   <button class="quiz-btn" data-choice="2">&lt;h2&gt;</button>
 *   <button class="quiz-btn" data-choice="3">&lt;h3&gt;</button>
 *   <p class="quiz-feedback"></p>
 * </div>
 */
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.quiz').forEach(quiz => {
    const correct  = String(quiz.dataset.correct);
    const feedback = quiz.querySelector('.quiz-feedback');
    const buttons  = quiz.querySelectorAll('.quiz-btn');

    buttons.forEach(btn => {
      btn.addEventListener('click', () => {
        buttons.forEach(b => b.disabled = true);
        const chosen = String(btn.dataset.choice);

        if (chosen === correct) {
          btn.classList.add('quiz-correct');
          feedback.textContent = quiz.dataset.win  || '🎉 Yes! That\'s right!';
          feedback.className   = 'quiz-feedback quiz-win';
        } else {
          btn.classList.add('quiz-wrong');
          feedback.textContent = quiz.dataset.lose || '😅 Not quite — try the next question!';
          feedback.className   = 'quiz-feedback quiz-lose';
          // highlight correct
          buttons.forEach(b => {
            if (String(b.dataset.choice) === correct) b.classList.add('quiz-correct');
          });
        }
      });
    });
  });
});

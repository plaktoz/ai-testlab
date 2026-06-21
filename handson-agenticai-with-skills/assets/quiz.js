/* ============================================================
   Quiz Widget — Agentic AI Course
   Usage: data-correct="b" on .quiz-question, data-key="a/b/c/d" on .quiz-option
   ============================================================ */

(function () {
  function initQuiz(container) {
    const questions = container.querySelectorAll('.quiz-question');

    questions.forEach(function (q) {
      const correct = q.dataset.correct;
      const feedback = q.querySelector('.quiz-feedback');
      const options = q.querySelectorAll('.quiz-option');
      let answered = false;

      options.forEach(function (btn) {
        btn.addEventListener('click', function () {
          if (answered) return;
          answered = true;

          const chosen = btn.dataset.key;

          options.forEach(function (b) {
            b.style.pointerEvents = 'none';
            if (b.dataset.key === correct) {
              b.classList.add('correct');
            }
          });

          if (chosen === correct) {
            btn.classList.add('correct');
            if (feedback) feedback.textContent = '✓ Correct!';
          } else {
            btn.classList.add('wrong');
            if (feedback) feedback.textContent = '✗ Not quite — the highlighted option is correct.';
          }
        });
      });
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.quiz').forEach(initQuiz);
  });
})();

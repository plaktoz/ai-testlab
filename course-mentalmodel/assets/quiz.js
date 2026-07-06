/**
 * quiz.js — reusable quiz widget for mental models course
 *
 * Usage: call initQuiz() after DOM is loaded.
 * Each .quiz block needs:
 *   data-correct="index"   (0-based index of the correct .quiz__option)
 *   data-correct-msg       (text shown on correct answer)
 *   data-wrong-msg         (text shown on wrong answer)
 */

function initQuiz() {
  document.querySelectorAll('.quiz').forEach(quiz => {
    const correctIdx = parseInt(quiz.dataset.correct, 10);
    const correctMsg = quiz.dataset.correctMsg || '✓ Correct!';
    const wrongMsg   = quiz.dataset.wrongMsg   || '✗ Not quite. Try again!';

    const options    = quiz.querySelectorAll('.quiz__option');
    const feedback   = quiz.querySelector('.quiz__feedback');
    const retryBtn   = quiz.querySelector('.quiz__retry');
    const scoreEl    = quiz.querySelector('.quiz__score');

    let attempts = 0;

    function reset() {
      options.forEach(o => {
        o.classList.remove('correct', 'wrong');
        o.disabled = false;
      });
      feedback.className = 'quiz__feedback';
      feedback.textContent = '';
      retryBtn.classList.remove('show');
    }

    options.forEach((option, idx) => {
      option.addEventListener('click', () => {
        attempts++;
        options.forEach(o => o.disabled = true);

        if (idx === correctIdx) {
          option.classList.add('correct');
          feedback.textContent = correctMsg;
          feedback.className = 'quiz__feedback correct show';
          retryBtn.classList.remove('show');
          if (scoreEl) scoreEl.textContent = attempts === 1
            ? '🎉 Got it on the first try!'
            : `Got it in ${attempts} attempt${attempts > 1 ? 's' : ''}.`;
        } else {
          option.classList.add('wrong');
          feedback.textContent = wrongMsg;
          feedback.className = 'quiz__feedback wrong show';
          retryBtn.classList.add('show');
          if (scoreEl) scoreEl.textContent = `Attempts so far: ${attempts}`;
        }
      });
    });

    if (retryBtn) {
      retryBtn.addEventListener('click', reset);
    }
  });
}

document.addEventListener('DOMContentLoaded', initQuiz);

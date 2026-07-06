/**
 * Money School — Quiz Engine
 * Handles all interactive quiz logic across lessons.
 */

(function () {
  'use strict';

  /**
   * Initialise all quiz blocks on the page.
   * Each .quiz-block must contain:
   *   - data-correct="index"  (0-based index of correct .quiz-btn)
   *   - .quiz-btn buttons
   *   - .feedback-box elements (first = correct msg, second = wrong msg)
   *   - optional .score-box (one per .quiz-section) for final tally
   */
  function initQuizzes() {
    const sections = document.querySelectorAll('.quiz-section');

    sections.forEach(function (section) {
      const blocks   = section.querySelectorAll('.quiz-block');
      const scoreBox = section.querySelector('.score-box');
      let answered   = 0;
      let correct    = 0;

      blocks.forEach(function (block, blockIdx) {
        const correctIdx  = parseInt(block.dataset.correct, 10);
        const buttons     = block.querySelectorAll('.quiz-btn');
        const feedbacks   = block.querySelectorAll('.feedback-box');
        const correctFb   = feedbacks[0];
        const wrongFb     = feedbacks[1];

        buttons.forEach(function (btn, btnIdx) {
          btn.addEventListener('click', function () {
            // Prevent double-answering
            if (block.dataset.answered) return;
            block.dataset.answered = '1';

            buttons.forEach(function (b) { b.disabled = true; });

            if (btnIdx === correctIdx) {
              btn.classList.add('correct');
              if (correctFb) { correctFb.classList.add('show', 'correct'); }
              correct++;
            } else {
              btn.classList.add('wrong');
              buttons[correctIdx].classList.add('correct');
              if (wrongFb) { wrongFb.classList.add('show', 'wrong'); }
            }

            answered++;

            if (scoreBox && answered === blocks.length) {
              showScore(scoreBox, correct, blocks.length);
            }
          });
        });
      });
    });
  }

  function showScore(box, correct, total) {
    const pct     = Math.round((correct / total) * 100);
    const numEl   = box.querySelector('.score-number');
    const msgEl   = box.querySelector('.score-msg');

    if (numEl) numEl.textContent = correct + ' / ' + total;

    if (msgEl) {
      if (pct === 100) {
        msgEl.textContent = '🎉 Amazing! You got them ALL right! You are a money superstar! 🌟';
      } else if (pct >= 75) {
        msgEl.textContent = '👏 Great job! You know a lot about money! Try the ones you missed again soon.';
      } else if (pct >= 50) {
        msgEl.textContent = '😊 Good try! Read through the lesson again and you will get even better!';
      } else {
        msgEl.textContent = '💪 Keep practising! Every time you try, you learn more. You can do it!';
      }
    }

    box.classList.add('show');
    box.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  }

  document.addEventListener('DOMContentLoaded', initQuizzes);
})();

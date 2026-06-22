document.addEventListener('DOMContentLoaded', function () {
  document.querySelectorAll('.quiz-block').forEach(function (block) {
    var correct = block.dataset.correct;
    var answered = false;

    block.querySelectorAll('.quiz-btn').forEach(function (btn) {
      btn.addEventListener('click', function () {
        if (answered) return;
        answered = true;

        block.querySelectorAll('.quiz-btn').forEach(function (b) {
          b.disabled = true;
          if (b.dataset.value === correct) {
            b.classList.add('correct');
          } else if (b === btn && b.dataset.value !== correct) {
            b.classList.add('incorrect');
          }
        });

        var exp = block.querySelector('.quiz-explanation');
        if (exp) exp.style.display = 'block';
      });
    });
  });
});

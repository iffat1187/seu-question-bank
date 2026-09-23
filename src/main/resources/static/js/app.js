/* SEU Question Bank — app.js : vanilla JS for search + UI */
(function () {
    'use strict';

    // Dismiss alerts on click X
    document.addEventListener('click', function (e) {
        const btn = e.target.closest('[data-dismiss-alert]');
        if (btn) {
            const alert = btn.closest('.alert');
            if (alert) alert.remove();
        }
    });

    // Course client-side search
    function initCourseSearch() {
        const input = document.getElementById('courseSearchInput');
        const countEl = document.getElementById('courseCount');
        const noResults = document.getElementById('courseNoResults');
        if (!input) return;

        const rows = Array.from(document.querySelectorAll('[data-course-row]'));
        const cards = Array.from(document.querySelectorAll('[data-course-card]'));
        const allItems = rows.length ? rows : cards;
        if (allItems.length === 0) return;

        const totalLabel = countEl ? countEl.dataset.total : null;

        function filter() {
            const q = input.value.trim().toLowerCase();
            let visible = 0;
            allItems.forEach(el => {
                const code = (el.dataset.code || '').toLowerCase();
                const title = (el.dataset.title || '').toLowerCase();
                const hay = code + ' ' + title;
                const match = !q || hay.includes(q);
                el.style.display = match ? '' : 'none';
                if (match) visible++;
            });
            if (countEl) {
                if (q) {
                    countEl.textContent = 'Showing ' + visible + ' of ' + allItems.length + ' courses';
                } else {
                    countEl.textContent = totalLabel || (allItems.length + ' courses');
                }
            }
            if (noResults) {
                noResults.style.display = visible === 0 ? '' : 'none';
            }
            // toggle table wrapper visibility if no rows
            const wrapper = document.getElementById('courseTableWrapper');
            if (wrapper && rows.length) {
                wrapper.style.display = visible === 0 ? 'none' : '';
            }
        }

        input.addEventListener('input', filter);

        const clearBtn = document.getElementById('clearSearchBtn');
        if (clearBtn) {
            clearBtn.addEventListener('click', function () {
                input.value = '';
                filter();
                input.focus();
            });
        }

        // If user came with ?q= param, init filter immediately
        if (input.value.trim()) filter();
    }

    // Home hero search -> redirect to /courses?q=
    function initHomeSearch() {
        const form = document.getElementById('homeSearchForm');
        if (!form) return;
        form.addEventListener('submit', function (e) {
            const input = form.querySelector('input[name="q"]');
            if (!input || !input.value.trim()) {
                e.preventDefault();
                window.location.href = '/courses';
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', function () {
            initCourseSearch();
            initHomeSearch();
        });
    } else {
        initCourseSearch();
        initHomeSearch();
    }
})();

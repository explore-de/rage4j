// Rage4J Demo Application

class Rage4JDemo {
    constructor() {
        this.apiBase = '';
        this.metrics = [];
        this.examples = [];
        this.selectedMetrics = new Set();

        this.init();
    }

    async init() {
        this.bindElements();
        this.bindEvents();
        await this.checkHealth();
        await this.loadMetrics();
        await this.loadExamples();
    }

    bindElements() {
        this.apiStatus = document.getElementById('apiStatus');
        this.statusDot = this.apiStatus.querySelector('.status-dot');
        this.statusText = this.apiStatus.querySelector('.status-text');
        this.exampleSelect = document.getElementById('exampleSelect');
        this.questionInput = document.getElementById('question');
        this.answerInput = document.getElementById('answer');
        this.groundTruthInput = document.getElementById('groundTruth');
        this.contextsInput = document.getElementById('contexts');
        this.metricsGrid = document.getElementById('metricsGrid');
        this.evaluateBtn = document.getElementById('evaluateBtn');
        this.clearBtn = document.getElementById('clearBtn');
        this.resultsGrid = document.getElementById('resultsGrid');
        this.resultsSummary = document.getElementById('resultsSummary');
        this.loadingOverlay = document.getElementById('loadingOverlay');
    }

    bindEvents() {
        this.evaluateBtn.addEventListener('click', () => this.evaluate());
        this.clearBtn.addEventListener('click', () => this.clear());
        this.exampleSelect.addEventListener('change', (e) => this.loadExample(e.target.value));
    }

    async checkHealth() {
        try {
            const response = await fetch(`${this.apiBase}/api/health`);
            const data = await response.json();

            if (data.configured) {
                this.statusDot.className = 'status-dot connected';
                this.statusText.textContent = 'API Connected';
            } else {
                this.statusDot.className = 'status-dot error';
                this.statusText.textContent = 'API Key Missing';
            }
        } catch (error) {
            this.statusDot.className = 'status-dot error';
            this.statusText.textContent = 'API Unavailable';
        }
    }

    async loadMetrics() {
        try {
            const response = await fetch(`${this.apiBase}/api/metrics`);
            this.metrics = await response.json();
            this.renderMetrics();
        } catch (error) {
            console.error('Failed to load metrics:', error);
        }
    }

    async loadExamples() {
        try {
            const response = await fetch(`${this.apiBase}/api/examples`);
            this.examples = await response.json();
            this.renderExamples();
        } catch (error) {
            console.error('Failed to load examples:', error);
        }
    }

    renderMetrics() {
        this.metricsGrid.innerHTML = this.metrics.map(metric => `
            <label class="metric-card selected" data-metric="${metric.id}">
                <input type="checkbox" value="${metric.id}" checked>
                <div class="metric-name">
                    <span class="metric-check"></span>
                    ${metric.name}
                </div>
                <div class="metric-desc">${metric.description}</div>
            </label>
        `).join('');

        // Initialize selected metrics
        this.metrics.forEach(m => this.selectedMetrics.add(m.id));

        // Bind click events
        this.metricsGrid.querySelectorAll('.metric-card').forEach(card => {
            card.addEventListener('click', () => {
                const metricId = card.dataset.metric;
                const checkbox = card.querySelector('input');

                checkbox.checked = !checkbox.checked;
                card.classList.toggle('selected', checkbox.checked);

                if (checkbox.checked) {
                    this.selectedMetrics.add(metricId);
                } else {
                    this.selectedMetrics.delete(metricId);
                }
            });
        });
    }

    renderExamples() {
        this.examples.forEach((example, index) => {
            const option = document.createElement('option');
            option.value = index;
            option.textContent = example.name;
            this.exampleSelect.appendChild(option);
        });
    }

    loadExample(index) {
        if (index === '') return;

        const example = this.examples[index];
        if (!example) return;

        this.questionInput.value = example.question || '';
        this.answerInput.value = example.answer || '';
        this.groundTruthInput.value = example.groundTruth || '';
        this.contextsInput.value = (example.contexts || []).join('\n');
    }

    clear() {
        this.questionInput.value = '';
        this.answerInput.value = '';
        this.groundTruthInput.value = '';
        this.contextsInput.value = '';
        this.exampleSelect.value = '';
        this.resultsGrid.innerHTML = `
            <div class="results-placeholder">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                    <path d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/>
                </svg>
                <p>Run an evaluation to see results here</p>
            </div>
        `;
        this.resultsSummary.innerHTML = '';
    }

    async evaluate() {
        if (this.selectedMetrics.size === 0) {
            alert('Please select at least one metric to evaluate.');
            return;
        }

        const question = this.questionInput.value.trim();
        const answer = this.answerInput.value.trim();

        if (!question || !answer) {
            alert('Please enter at least a question and an answer.');
            return;
        }

        const request = {
            question: question,
            answer: answer,
            groundTruth: this.groundTruthInput.value.trim() || null,
            contexts: this.contextsInput.value.trim()
                ? this.contextsInput.value.trim().split('\n').filter(c => c.trim())
                : [],
            metrics: Array.from(this.selectedMetrics)
        };

        this.showLoading(true);

        try {
            const response = await fetch(`${this.apiBase}/api/evaluate`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(request)
            });

            const results = await response.json();
            this.renderResults(results);
        } catch (error) {
            console.error('Evaluation failed:', error);
            this.renderResults([{
                metric: 'Error',
                status: 'error',
                error: 'Failed to connect to the evaluation service. Please check if the server is running.'
            }]);
        } finally {
            this.showLoading(false);
        }
    }

    renderResults(results) {
        const successResults = results.filter(r => r.status === 'success');
        const avgScore = successResults.length > 0
            ? (successResults.reduce((sum, r) => sum + r.score, 0) / successResults.length)
            : 0;
        const totalTime = results.reduce((sum, r) => sum + (r.executionTimeMs || 0), 0);

        this.resultsSummary.innerHTML = `
            <span>Average Score: <strong>${(avgScore * 100).toFixed(1)}%</strong></span>
            <span>Total Time: <strong>${totalTime}ms</strong></span>
        `;

        this.resultsGrid.innerHTML = results.map((result, index) => {
            if (result.status === 'error') {
                return `
                    <div class="result-card error" style="animation-delay: ${index * 0.1}s">
                        <div class="result-header">
                            <span class="result-metric">${result.metric}</span>
                        </div>
                        <div class="result-error">${result.error}</div>
                    </div>
                `;
            }

            const scorePercent = (result.score * 100).toFixed(1);
            const scoreClass = result.score >= 0.7 ? 'high' : result.score >= 0.4 ? 'medium' : 'low';

            return `
                <div class="result-card" style="animation-delay: ${index * 0.1}s">
                    <div class="result-header">
                        <span class="result-metric">${result.metric}</span>
                        <span class="result-time">${result.executionTimeMs}ms</span>
                    </div>
                    <div class="result-score">
                        <div class="score-bar">
                            <div class="score-fill ${scoreClass}" style="width: ${scorePercent}%"></div>
                        </div>
                        <div class="score-value">
                            <span class="score-number ${scoreClass}">${scorePercent}%</span>
                            <span class="score-label">${this.getScoreLabel(result.score)}</span>
                        </div>
                    </div>
                    <div class="result-desc">${result.description}</div>
                </div>
            `;
        }).join('');
    }

    getScoreLabel(score) {
        if (score >= 0.9) return 'Excellent';
        if (score >= 0.7) return 'Good';
        if (score >= 0.5) return 'Fair';
        if (score >= 0.3) return 'Poor';
        return 'Very Poor';
    }

    showLoading(show) {
        this.loadingOverlay.classList.toggle('active', show);
    }
}

// Initialize the app
document.addEventListener('DOMContentLoaded', () => {
    window.app = new Rage4JDemo();
});

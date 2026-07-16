/**
 * MovieTicket - Interactive Seat Map
 */

class SeatMap {
  constructor(containerId, options = {}) {
    this.container = document.getElementById(containerId);
    if (!this.container) {
      console.error('Seat map container not found:', containerId);
      return;
    }
    this.seats = [];
    this.selectedSeats = [];
    this.vipSeats = options.vipSeats || [];
    this.bookedSeats = options.bookedSeats || [];
    this.tempBookedSeats = options.tempBookedSeats || [];
    this.rows = options.rows || 10;
    this.cols = options.cols || 14;
    this.seatPrice = options.seatPrice || 10;
    this.vipPrice = options.vipPrice || 15;
    this.selectedCountEl = document.getElementById('selected-count');
    this.totalPriceEl = document.getElementById('total-price');
    this.selectedSeatsEl = document.getElementById('selected-seats-list');
    this.continueBtn = document.getElementById('continue-btn');
    this.hallId = options.hallId || null;
    this.showtimeId = options.showtimeId || null;
    this.pollInterval = null;
    this.pollingActive = false;

    this.init();
  }

  init() {
    this.renderSeatGrid();
    this.bindEvents();
    if (this.hallId && this.showtimeId) {
      this.startPolling();
    }
    this.updateSummary();
  }

  renderSeatGrid() {
    const grid = this.container.querySelector('.seat-grid');
    if (!grid) return;
    grid.innerHTML = '';

    for (let r = 0; r < this.rows; r++) {
      const rowLabel = String.fromCharCode(65 + r);
      const rowDiv = document.createElement('div');
      rowDiv.className = 'seat-row';
      const labelSpan = document.createElement('span');
      labelSpan.className = 'seat-label';
      labelSpan.textContent = rowLabel;
      rowDiv.appendChild(labelSpan);

      for (let c = 1; c <= this.cols; c++) {
        const seat = document.createElement('div');
        const seatId = `${rowLabel}${c}`;
        const seatNum = document.createElement('span');
        seatNum.className = 'seat-number';
        seatNum.textContent = c;
        seat.appendChild(seatNum);

        seat.className = 'seat available';
        seat.dataset.row = rowLabel;
        seat.dataset.col = c;
        seat.dataset.seatNumber = seatId;
        seat.dataset.price = this.seatPrice;

        if (this.vipSeats.includes(seatId)) {
          seat.classList.add('vip');
          seat.dataset.price = this.vipPrice;
        }

        if (this.bookedSeats.includes(seatId) || this.tempBookedSeats.includes(seatId)) {
          seat.classList.remove('available');
          seat.classList.add('booked');
        }

        if (this.selectedSeats.includes(seatId)) {
          seat.classList.remove('available');
          seat.classList.add('selected');
        }

        rowDiv.appendChild(seat);
      }
      grid.appendChild(rowDiv);
    }
  }

  bindEvents() {
    this.container.addEventListener('click', (e) => {
      const seat = e.target.closest('.seat');
      if (!seat) return;
      if (seat.classList.contains('booked')) return;

      const seatNumber = seat.dataset.seatNumber;

      if (seat.classList.contains('selected')) {
        seat.classList.remove('selected');
        seat.classList.add('available');
        this.selectedSeats = this.selectedSeats.filter(s => s !== seatNumber);
      } else {
        seat.classList.remove('available');
        seat.classList.add('selected');
        this.selectedSeats.push(seatNumber);
      }
      this.updateSummary();
    });
  }

  updateSummary() {
    const count = this.selectedSeats.length;
    let total = 0;
    const seatNames = [];

    this.selectedSeats.forEach(seatId => {
      const seatEl = this.container.querySelector(`[data-seat-number="${seatId}"]`);
      const price = seatEl ? parseFloat(seatEl.dataset.price) : this.seatPrice;
      total += price;
      seatNames.push(seatId);
    });

    if (this.selectedCountEl) this.selectedCountEl.textContent = count;
    if (this.totalPriceEl) this.totalPriceEl.textContent = formatCurrency(total);
    if (this.selectedSeatsEl) {
      this.selectedSeatsEl.innerHTML = seatNames.join(', ');
    }
    if (this.continueBtn) {
      this.continueBtn.disabled = count === 0;
    }

    // Update hidden form fields
    const seatsInput = document.getElementById('selectedSeats');
    if (seatsInput) seatsInput.value = seatNames.join(',');
    const totalInput = document.getElementById('totalAmount');
    if (totalInput) totalInput.value = total.toFixed(2);
  }

  getSelectedSeats() {
    return this.selectedSeats;
  }

  clearSelection() {
    this.selectedSeats = [];
    this.container.querySelectorAll('.seat.selected').forEach(seat => {
      seat.classList.remove('selected');
      seat.classList.add('available');
    });
    this.updateSummary();
  }

  // Real-time availability polling
  startPolling() {
    if (this.pollingActive) return;
    this.pollingActive = true;
    this.fetchAvailability();
    this.pollInterval = setInterval(() => this.fetchAvailability(), 30000);
  }

  stopPolling() {
    this.pollingActive = false;
    if (this.pollInterval) {
      clearInterval(this.pollInterval);
      this.pollInterval = null;
    }
  }

  async fetchAvailability() {
    try {
      const url = `/api/bookings/seats?showtimeId=${this.showtimeId}&hallId=${this.hallId}`;
      const response = await fetch(url);
      if (!response.ok) return;
      const data = await response.json();
      this.updateAvailability(data);
    } catch (error) {
      console.error('Seat availability fetch failed:', error);
    }
  }

  updateAvailability(data) {
    const newlyBooked = data.bookedSeats || [];
    const currentlySelected = [...this.selectedSeats];

    this.container.querySelectorAll('.seat').forEach(seat => {
      const seatNumber = seat.dataset.seatNumber;
      if (seat.classList.contains('selected')) return;

      if (newlyBooked.includes(seatNumber)) {
        seat.classList.remove('available');
        seat.classList.add('booked');
      }
    });

    currentlySelected.forEach(seatId => {
      if (newlyBooked.includes(seatId)) {
        this.showSeatTakenAlert(seatId);
        this.selectedSeats = this.selectedSeats.filter(s => s !== seatId);
        const seatEl = this.container.querySelector(`[data-seat-number="${seatId}"]`);
        if (seatEl) {
          seatEl.classList.remove('selected');
          seatEl.classList.add('booked');
        }
      }
    });
    this.updateSummary();
  }

  showSeatTakenAlert(seatId) {
    toast(`Seat ${seatId} was just taken by someone else!`, 'warning');
  }

  destroy() {
    this.stopPolling();
    this.container.removeEventListener('click', this.boundClickHandler);
  }
}

// Initialize seat map on page load
document.addEventListener('DOMContentLoaded', function() {
  const seatMapContainer = document.getElementById('seat-map');
  if (seatMapContainer) {
    const seatMapData = window.seatMapConfig || {};
    new SeatMap('seat-map', seatMapData);
  }
});

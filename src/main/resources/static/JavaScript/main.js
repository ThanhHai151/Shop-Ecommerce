
document.addEventListener('DOMContentLoaded', ()=>{
  function setCartCount(count) {
    const n = Number(count);
    if (!Number.isFinite(n)) return;

    const cartBadge = document.querySelector('.cart-count');
    if (!cartBadge) return;

    cartBadge.textContent = String(n);
    if (n <= 0) cartBadge.classList.add('is-empty');
    else cartBadge.classList.remove('is-empty');
  }

  // Expose global so inline scripts (e.g. product-detail.html) can call it.
  window.updateCartCount = function updateCartCount() {
    return fetch('/cart/count', { headers: { 'Accept': 'application/json' } })
      .then(r => r.json())
      .then(data => {
        if (data && (data.count !== undefined && data.count !== null)) {
          setCartCount(data.count);
        }
      })
      .catch(() => {
        // Ignore; cart count is non-critical UI.
      });
  };

  // Always sync cart count from server on page load
  window.updateCartCount();

  const searchForm = document.getElementById('search-form');
  if(searchForm){
    searchForm.addEventListener('submit', e=>{
      e.preventDefault();
      const q = document.getElementById('q').value.trim();
      if(!q) return;
      
      console.log('Search for:', q);
      window.location.href = '/search?q=' + encodeURIComponent(q);
    });
  }

  document.querySelectorAll('.add-to-cart-btn').forEach(btn=>{
    btn.addEventListener('click', e=>{
      e.preventDefault();
      const productId = btn.dataset.id;
      const quantity = 1;
      
      // Disable button during request
      const originalText = btn.innerHTML;
      btn.disabled = true;
      btn.innerHTML = 'Đang thêm...';
      
      // Call backend API
      fetch('/cart/add', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: `productId=${productId}&quantity=${quantity}`
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          btn.innerHTML = 'Đã thêm';
          btn.classList.add('btn-success');
          btn.classList.remove('btn-primary');
          
          // Update cart count in header immediately.
          const nextCount =
            (data.itemCount ?? data.cartCount ?? data.count ?? data.totalItems);
          if (nextCount !== undefined && nextCount !== null) {
            setCartCount(nextCount);
          } else if (typeof window.updateCartCount === 'function') {
            window.updateCartCount();
          }
          
          // Reset button after 2 seconds
          setTimeout(() => {
            btn.innerHTML = originalText;
            btn.disabled = false;
            btn.classList.remove('btn-success');
            btn.classList.add('btn-primary');
          }, 2000);
        } else {
          alert(data.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
          btn.innerHTML = originalText;
          btn.disabled = false;
        }
      })
      .catch(error => {
        console.error('Error:', error);
        alert('Có lỗi xảy ra khi thêm vào giỏ hàng. Vui lòng đăng nhập.');
        btn.innerHTML = originalText;
        btn.disabled = false;
      });
    })
  })
});

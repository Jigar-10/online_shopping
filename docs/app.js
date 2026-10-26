// NovaMart Online Shopping DBMS Interactive Database & Storefront
const DB = {
  categories: [
    { category_id: 1, name: 'Electronics' },
    { category_id: 2, name: 'Clothing' },
    { category_id: 3, name: 'Books' },
    { category_id: 4, name: 'Home Appliances' },
    { category_id: 5, name: 'Accessories' }
  ],
  suppliers: [
    { supplier_id: 1, name: 'Tech Suppliers', contact_email: 'tech@example.com', phone: '9876543210' },
    { supplier_id: 2, name: 'Fashion Traders', contact_email: 'fashion@example.com', phone: '9876543211' },
    { supplier_id: 3, name: 'Book Distributors', contact_email: 'books@example.com', phone: '9876543212' },
    { supplier_id: 4, name: 'Home Essentials', contact_email: 'home@example.com', phone: '9876543213' },
    { supplier_id: 5, name: 'Accessory World', contact_email: 'accessories@example.com', phone: '9876543214' }
  ],
  customers: [
    { customer_id: 1, name: 'Aarav Sharma', email: 'aarav@gmail.com', phone: '9876500001', registered_on: '2026-01-10', gender: 'Male' },
    { customer_id: 2, name: 'Priya Patel', email: 'priya@gmail.com', phone: '9876500002', registered_on: '2026-01-12', gender: 'Female' },
    { customer_id: 3, name: 'Rahul Mehta', email: 'rahul@gmail.com', phone: '9876500003', registered_on: '2026-01-15', gender: 'Male' },
    { customer_id: 4, name: 'Sneha Shah', email: 'sneha@gmail.com', phone: '9876500004', registered_on: '2026-01-18', gender: 'Female' },
    { customer_id: 5, name: 'Arjun Verma', email: 'arjun@gmail.com', phone: '9876500005', registered_on: '2026-01-20', gender: 'Male' }
  ],
  addresses: [
    { address_id: 1, customer_id: 1, street: 'MG Road', city: 'Mumbai', state: 'Maharashtra', country: 'India', pincode: '400001' },
    { address_id: 2, customer_id: 2, street: 'FC Road', city: 'Pune', state: 'Maharashtra', country: 'India', pincode: '411004' },
    { address_id: 3, customer_id: 3, street: 'Ring Road', city: 'Surat', state: 'Gujarat', country: 'India', pincode: '395003' },
    { address_id: 4, customer_id: 4, street: 'Park Street', city: 'Kolkata', state: 'West Bengal', country: 'India', pincode: '700016' },
    { address_id: 5, customer_id: 5, street: 'Brigade Road', city: 'Bengaluru', state: 'Karnataka', country: 'India', pincode: '560001' }
  ],
  products: [
    { product_id: 1, category_id: 1, supplier_id: 1, name: 'Laptop', description: 'Portable sleek computer with Intel Core i7, 16GB RAM, 512GB SSD', stock_qty: 20, price: 55000.00, img: 'images/laptop.jpg' },
    { product_id: 2, category_id: 2, supplier_id: 2, name: 'T-Shirt', description: 'Premium 100% breathable combed cotton casual daily-wear t-shirt', stock_qty: 100, price: 799.00, img: 'images/tshirt.jpg' },
    { product_id: 3, category_id: 3, supplier_id: 3, name: 'SQL Book', description: 'Database Systems & Relational Theory textbook for CS students', stock_qty: 50, price: 499.00, img: 'images/book.jpg' },
    { product_id: 4, category_id: 4, supplier_id: 4, name: 'Mixer', description: 'Kitchen culinary mixer blender appliance with 750W motor', stock_qty: 15, price: 2499.00, img: 'images/mixer.jpg' },
    { product_id: 5, category_id: 5, supplier_id: 5, name: 'Headphones', description: 'Wireless noise-canceling over-ear studio sound headphones', stock_qty: 30, price: 1999.00, img: 'images/headphones.jpg' }
  ],
  reviews: [
    { review_id: 1, customer_id: 1, product_id: 1, rating: 5, comment: 'Excellent performance and build quality!' },
    { review_id: 2, customer_id: 2, product_id: 2, rating: 4, comment: 'Good quality fabric, comfortable fit.' },
    { review_id: 3, customer_id: 3, product_id: 3, rating: 5, comment: 'Very useful book for DBMS coursework.' },
    { review_id: 4, customer_id: 4, product_id: 4, rating: 3, comment: 'Average product, works as expected.' },
    { review_id: 5, customer_id: 5, product_id: 5, rating: 4, comment: 'Good sound quality and battery life.' }
  ],
  orders: [
    { order_id: 1, customer_id: 1, shipment_id: 1, order_date: '2026-02-01 10:30:00', total_amount: 55000.00, status: 'Delivered' },
    { order_id: 2, customer_id: 2, shipment_id: 2, order_date: '2026-02-02 11:15:00', total_amount: 1598.00, status: 'Shipped' },
    { order_id: 3, customer_id: 3, shipment_id: 3, order_date: '2026-02-03 14:00:00', total_amount: 499.00, status: 'Pending' },
    { order_id: 4, customer_id: 4, shipment_id: 4, order_date: '2026-02-04 16:20:00', total_amount: 2499.00, status: 'Shipped' },
    { order_id: 5, customer_id: 5, shipment_id: 5, order_date: '2026-02-05 09:45:00', total_amount: 3998.00, status: 'Delivered' }
  ],
  order_items: [
    { order_id: 1, product_id: 1, quantity: 1, unit_price: 55000.00 },
    { order_id: 2, product_id: 2, quantity: 2, unit_price: 799.00 },
    { order_id: 3, product_id: 3, quantity: 1, unit_price: 499.00 },
    { order_id: 4, product_id: 4, quantity: 1, unit_price: 2499.00 },
    { order_id: 5, product_id: 5, quantity: 2, unit_price: 1999.00 }
  ],
  shipping: [
    { shipment_id: 1, address_id: 1, carrier: 'BlueDart', tracking_no: 'TRK1001', status: 'Delivered' },
    { shipment_id: 2, address_id: 2, carrier: 'DTDC', tracking_no: 'TRK1002', status: 'Shipped' },
    { shipment_id: 3, address_id: 3, carrier: 'Delhivery', tracking_no: 'TRK1003', status: 'Pending' },
    { shipment_id: 4, address_id: 4, carrier: 'IndiaPost', tracking_no: 'TRK1004', status: 'Shipped' },
    { shipment_id: 5, address_id: 5, carrier: 'BlueDart', tracking_no: 'TRK1005', status: 'Delivered' }
  ],
  payments: [
    { payment_id: 1, order_id: 1, amount: 55000.00, payment_type: 'Credit Card', status: 'Completed' },
    { payment_id: 2, order_id: 2, amount: 1598.00, payment_type: 'Credit Card', status: 'Completed' },
    { payment_id: 3, order_id: 3, amount: 499.00, payment_type: 'Credit Card', status: 'Pending' },
    { payment_id: 4, order_id: 4, amount: 2499.00, payment_type: 'Credit Card', status: 'Completed' },
    { payment_id: 5, order_id: 5, amount: 3998.00, payment_type: 'Credit Card', status: 'Completed' }
  ],
  credit_cards: [
    { payment_id: 1, card_number: '**** **** **** 1234', cvv: '***', expiry_date: '2028-12-31' },
    { payment_id: 2, card_number: '**** **** **** 5678', cvv: '***', expiry_date: '2027-08-31' },
    { payment_id: 3, card_number: '**** **** **** 9012', cvv: '***', expiry_date: '2029-05-31' },
    { payment_id: 4, card_number: '**** **** **** 3456', cvv: '***', expiry_date: '2028-02-28' },
    { payment_id: 5, card_number: '**** **** **** 7890', cvv: '***', expiry_date: '2027-11-30' }
  ],
  digital_wallets: [
    { payment_id: 6, provider: 'Paytm', wallet_ref: 'WLT-PAYTM-8821' },
    { payment_id: 7, provider: 'PhonePe', wallet_ref: 'WLT-PHONEPE-9943' }
  ],
  carts: [
    { cart_id: 1, customer_id: 1 },
    { cart_id: 2, customer_id: 2 },
    { cart_id: 3, customer_id: 3 },
    { cart_id: 4, customer_id: 4 },
    { cart_id: 5, customer_id: 5 }
  ],
  cart_items: [
    { cart_id: 1, product_id: 2, quantity: 1 }
  ]
};

// State
let activeCustomerId = 1;
let currentCategory = 'all';
let searchQuery = '';
let currentSort = 'featured';
let activeAdminTable = 'products';

// Cart management
function getActiveCustomerCart() {
  return DB.cart_items.filter(item => item.cart_id === activeCustomerId);
}

function updateCartBadge() {
  const items = getActiveCustomerCart();
  const totalCount = items.reduce((sum, i) => sum + i.quantity, 0);
  const badge = document.getElementById('cart-badge');
  if (badge) badge.innerText = totalCount;
}

function renderProducts() {
  const grid = document.getElementById('products-grid');
  if (!grid) return;

  let filtered = DB.products.filter(p => {
    const matchCat = currentCategory === 'all' || p.category_id === parseInt(currentCategory);
    const matchSearch = p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
                        p.description.toLowerCase().includes(searchQuery.toLowerCase());
    return matchCat && matchSearch;
  });

  if (currentSort === 'price-low') {
    filtered.sort((a, b) => a.price - b.price);
  } else if (currentSort === 'price-high') {
    filtered.sort((a, b) => b.price - a.price);
  } else if (currentSort === 'name-az') {
    filtered.sort((a, b) => a.name.localeCompare(b.name));
  }

  grid.innerHTML = filtered.map(p => {
    const cat = DB.categories.find(c => c.category_id === p.category_id)?.name || 'General';
    const sup = DB.suppliers.find(s => s.supplier_id === p.supplier_id)?.name || 'Supplier';
    const inStock = p.stock_qty > 0;

    return `
      <div class="product-card">
        <div class="product-card-top">
          <span class="card-category">${cat}</span>
          <span class="card-stock ${inStock ? 'in-stock' : 'out-stock'}">
            ${inStock ? p.stock_qty + ' in stock' : 'Out of stock'}
          </span>
        </div>
        <div class="product-img-wrap">
          <img src="${p.img}" alt="${p.name}" loading="lazy" onerror="this.src='https://placehold.co/200x160/f1f5f9/64748b?text=${encodeURIComponent(p.name)}'"/>
        </div>
        <h3 class="product-title">${p.name}</h3>
        <p class="product-supplier">by ${sup}</p>
        <p class="product-desc">${p.description}</p>
        <div class="product-card-bottom">
          <div class="product-price">₹ ${p.price.toLocaleString('en-IN', {minimumFractionDigits: 2})}</div>
          <div class="card-actions">
            <button class="btn-secondary" onclick="openProductModal(${p.product_id})">Details</button>
            <button class="btn-primary" onclick="addToCart(${p.product_id})" ${!inStock ? 'disabled' : ''}>+ Cart</button>
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function addToCart(productId) {
  const product = DB.products.find(p => p.product_id === productId);
  if (!product || product.stock_qty <= 0) {
    alert('Sorry, this product is out of stock.');
    return;
  }

  let item = DB.cart_items.find(i => i.cart_id === activeCustomerId && i.product_id === productId);
  if (item) {
    if (item.quantity + 1 > product.stock_qty) {
      alert('Cannot add more than available stock.');
      return;
    }
    item.quantity++;
  } else {
    DB.cart_items.push({ cart_id: activeCustomerId, product_id: productId, quantity: 1 });
  }

  updateCartBadge();
  showToast(`Added ${product.name} to cart!`);
}

function openProductModal(productId) {
  const p = DB.products.find(p => p.product_id === productId);
  if (!p) return;

  const cat = DB.categories.find(c => c.category_id === p.category_id)?.name || 'General';
  const sup = DB.suppliers.find(s => s.supplier_id === p.supplier_id)?.name || 'Supplier';
  const reviews = DB.reviews.filter(r => r.product_id === productId);

  const modal = document.getElementById('modal-overlay');
  const content = document.getElementById('modal-body');

  content.innerHTML = `
    <div style="display: flex; gap: 20px; flex-wrap: wrap; margin-bottom: 20px;">
      <div style="width: 180px; height: 180px; border: 1px solid var(--border-color); border-radius: 8px; display: flex; align-items: center; justify-content: center; background: #fff;">
        <img src="${p.img}" style="max-width: 90%; max-height: 90%; object-fit: contain;"/>
      </div>
      <div style="flex: 1; min-width: 200px;">
        <span class="card-category">${cat}</span>
        <h2 style="font-size: 20px; font-weight: 700; margin-top: 4px;">${p.name}</h2>
        <p style="font-size: 13px; color: var(--text-muted);">Supplier: ${sup}</p>
        <div style="font-size: 22px; font-weight: 800; color: var(--primary); margin: 8px 0;">₹ ${p.price.toLocaleString('en-IN', {minimumFractionDigits: 2})}</div>
        <p style="font-size: 13px; color: ${p.stock_qty > 0 ? 'var(--success)' : 'var(--danger)'}; font-weight: 600;">
          ${p.stock_qty > 0 ? '● In Stock (' + p.stock_qty + ' available)' : '● Out of Stock'}
        </p>
        <button class="btn-primary" style="margin-top: 10px; width: 100%;" onclick="addToCart(${p.product_id}); closeModal();">Add to Cart</button>
      </div>
    </div>
    <div style="background: var(--bg-main); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color); margin-bottom: 20px;">
      <h4 style="font-size: 13px; font-weight: 700; margin-bottom: 4px;">Description & Specifications</h4>
      <p style="font-size: 13px; color: var(--text-muted);">${p.description}</p>
    </div>
    <h4 style="font-size: 14px; font-weight: 700; margin-bottom: 10px;">Customer Reviews (${reviews.length})</h4>
    <div style="max-height: 180px; overflow-y: auto; margin-bottom: 16px;">
      ${reviews.map(r => {
        const reviewer = DB.customers.find(c => c.customer_id === r.customer_id)?.name || 'Verified Shopper';
        return `
          <div style="border-bottom: 1px solid var(--border-color); padding: 8px 0;">
            <div style="display: flex; justify-content: space-between; font-size: 12px;">
              <strong>${reviewer}</strong>
              <span style="color: var(--warning); font-weight: 700;">${'★'.repeat(r.rating)}${'☆'.repeat(5 - r.rating)}</span>
            </div>
            <p style="font-size: 13px; color: var(--text-muted); margin-top: 2px;">${r.comment}</p>
          </div>
        `;
      }).join('') || '<p style="font-size: 13px; color: var(--text-muted);">No reviews yet for this product.</p>'}
    </div>
    <div style="background: var(--bg-subtle); padding: 12px; border-radius: 8px; border: 1px solid var(--border-color);">
      <h4 style="font-size: 13px; font-weight: 700; margin-bottom: 8px;">Leave a Verified Review</h4>
      <div style="display: flex; gap: 8px; margin-bottom: 8px;">
        <select id="review-rating" class="select-input" style="width: 110px;">
          <option value="5">★★★★★ (5)</option>
          <option value="4">★★★★☆ (4)</option>
          <option value="3">★★★☆☆ (3)</option>
          <option value="2">★★☆☆☆ (2)</option>
          <option value="1">★☆☆☆☆ (1)</option>
        </select>
        <input type="text" id="review-comment" class="search-input" style="flex: 1;" placeholder="Write your feedback..."/>
        <button class="btn-primary" onclick="submitReview(${p.product_id})">Post</button>
      </div>
    </div>
  `;

  document.getElementById('modal-title').innerText = `${p.name} - Product Details`;
  modal.classList.add('active');
}

function submitReview(productId) {
  const rating = parseInt(document.getElementById('review-rating').value);
  const comment = document.getElementById('review-comment').value.trim();
  if (!comment) {
    alert('Please enter a comment.');
    return;
  }

  DB.reviews.push({
    review_id: DB.reviews.length + 1,
    customer_id: activeCustomerId,
    product_id: productId,
    rating,
    comment
  });

  showToast('Thank you! Review published successfully.');
  openProductModal(productId);
  renderAdminTable();
}

function openCartModal() {
  const modal = document.getElementById('modal-overlay');
  const content = document.getElementById('modal-body');
  document.getElementById('modal-title').innerText = '🛒 Your Shopping Cart';

  const items = getActiveCustomerCart();
  if (items.length === 0) {
    content.innerHTML = `
      <div style="text-align: center; padding: 40px 20px;">
        <div style="font-size: 40px; margin-bottom: 10px;">🛍</div>
        <h3>Your Cart is Empty</h3>
        <p style="color: var(--text-muted); font-size: 13px; margin: 8px 0 16px;">Add some products from the catalog to get started.</p>
        <button class="btn-primary" onclick="closeModal()">Explore Catalog</button>
      </div>
    `;
    modal.classList.add('active');
    return;
  }

  let subtotal = 0;
  const itemsHtml = items.map(item => {
    const p = DB.products.find(p => p.product_id === item.product_id);
    const lineTotal = p.price * item.quantity;
    subtotal += lineTotal;

    return `
      <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px 0; border-bottom: 1px solid var(--border-color);">
        <div style="display: flex; align-items: center; gap: 12px;">
          <img src="${p.img}" style="width: 48px; height: 48px; object-fit: contain; border: 1px solid var(--border-color); border-radius: 6px; padding: 2px;"/>
          <div>
            <strong style="font-size: 14px;">${p.name}</strong>
            <div style="font-size: 12px; color: var(--text-muted);">₹ ${p.price.toLocaleString('en-IN')} each</div>
          </div>
        </div>
        <div style="display: flex; align-items: center; gap: 10px;">
          <button class="btn-secondary" style="padding: 2px 8px;" onclick="changeCartQty(${item.product_id}, -1)">-</button>
          <span style="font-weight: 700; font-size: 14px; min-width: 20px; text-align: center;">${item.quantity}</span>
          <button class="btn-secondary" style="padding: 2px 8px;" onclick="changeCartQty(${item.product_id}, 1)">+</button>
          <span style="font-weight: 700; font-size: 14px; min-width: 80px; text-align: right;">₹ ${lineTotal.toLocaleString('en-IN')}</span>
          <button class="btn-secondary" style="color: var(--danger); border-color: var(--danger);" onclick="removeFromCart(${item.product_id})">✕</button>
        </div>
      </div>
    `;
  }).join('');

  const customerAddresses = DB.addresses.filter(a => a.customer_id === activeCustomerId);

  content.innerHTML = `
    <div style="margin-bottom: 16px;">
      ${itemsHtml}
    </div>
    <div style="background: var(--bg-main); border: 1px solid var(--border-color); border-radius: 8px; padding: 14px; margin-bottom: 16px;">
      <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px;">
        <span>Subtotal</span>
        <span>₹ ${subtotal.toLocaleString('en-IN', {minimumFractionDigits: 2})}</span>
      </div>
      <div style="display: flex; justify-content: space-between; font-size: 13px; margin-bottom: 6px;">
        <span>Delivery / Shipping</span>
        <span style="color: var(--success); font-weight: 600;">FREE (Promo)</span>
      </div>
      <div style="display: flex; justify-content: space-between; font-size: 16px; font-weight: 800; border-top: 1px solid var(--border-color); padding-top: 8px;">
        <span>Total Payable</span>
        <span style="color: var(--primary);">₹ ${subtotal.toLocaleString('en-IN', {minimumFractionDigits: 2})}</span>
      </div>
    </div>

    <div style="margin-bottom: 16px;">
      <label style="display: block; font-size: 13px; font-weight: 700; margin-bottom: 6px;">Delivery Address</label>
      <select id="checkout-address" class="select-input" style="width: 100%;">
        ${customerAddresses.map(a => `
          <option value="${a.address_id}">${a.street}, ${a.city}, ${a.state} - ${a.pincode}</option>
        `).join('')}
      </select>
    </div>

    <div style="margin-bottom: 20px;">
      <label style="display: block; font-size: 13px; font-weight: 700; margin-bottom: 6px;">Payment Method</label>
      <div style="display: flex; gap: 10px;">
        <label style="font-size: 13px; display: flex; align-items: center; gap: 4px; cursor: pointer;">
          <input type="radio" name="pay-method" value="Credit Card" checked/> Credit Card
        </label>
        <label style="font-size: 13px; display: flex; align-items: center; gap: 4px; cursor: pointer;">
          <input type="radio" name="pay-method" value="Digital Wallet"/> Digital Wallet (Paytm/GPay)
        </label>
      </div>
    </div>

    <button class="btn-primary" style="width: 100%; padding: 10px; font-size: 14px;" onclick="executeCheckout(${subtotal})">
      ⚡ Place Order (Simulate ACID Transaction)
    </button>
  `;

  modal.classList.add('active');
}

function changeCartQty(productId, delta) {
  const item = DB.cart_items.find(i => i.cart_id === activeCustomerId && i.product_id === productId);
  if (!item) return;

  const product = DB.products.find(p => p.product_id === productId);
  if (delta > 0 && item.quantity + 1 > product.stock_qty) {
    alert('Maximum stock reached for this item.');
    return;
  }

  item.quantity += delta;
  if (item.quantity <= 0) {
    removeFromCart(productId);
  } else {
    updateCartBadge();
    openCartModal();
  }
}

function removeFromCart(productId) {
  DB.cart_items = DB.cart_items.filter(i => !(i.cart_id === activeCustomerId && i.product_id === productId));
  updateCartBadge();
  openCartModal();
}

function executeCheckout(totalAmount) {
  const items = getActiveCustomerCart();
  if (items.length === 0) return;

  const orderId = DB.orders.length + 1;
  const shipmentId = DB.shipping.length + 1;
  const addressId = parseInt(document.getElementById('checkout-address').value) || 1;
  const payType = document.querySelector('input[name="pay-method"]:checked').value;

  // 1. Create Shipment
  DB.shipping.push({
    shipment_id: shipmentId,
    address_id: addressId,
    carrier: 'BlueDart',
    tracking_no: 'TRK' + (1000 + shipmentId),
    status: 'Shipped'
  });

  // 2. Create Order
  DB.orders.push({
    order_id: orderId,
    customer_id: activeCustomerId,
    shipment_id: shipmentId,
    order_date: new Date().toISOString().replace('T', ' ').substring(0, 19),
    total_amount: totalAmount,
    status: 'Shipped'
  });

  // 3. Create Order Items & reduce product stock (simulating trigger reduce_product_stock)
  items.forEach(item => {
    const p = DB.products.find(p => p.product_id === item.product_id);
    DB.order_items.push({
      order_id: orderId,
      product_id: item.product_id,
      quantity: item.quantity,
      unit_price: p.price
    });
    p.stock_qty = Math.max(0, p.stock_qty - item.quantity);
  });

  // 4. Create Payment
  const paymentId = DB.payments.length + 1;
  DB.payments.push({
    payment_id: paymentId,
    order_id: orderId,
    amount: totalAmount,
    payment_type: payType,
    status: 'Completed'
  });

  if (payType === 'Credit Card') {
    DB.credit_cards.push({
      payment_id: paymentId,
      card_number: '**** **** **** 4492',
      cvv: '***',
      expiry_date: '2028-10-31'
    });
  } else {
    DB.digital_wallets.push({
      payment_id: paymentId,
      provider: 'Paytm',
      wallet_ref: 'WLT-' + Math.floor(100000 + Math.random() * 900000)
    });
  }

  // 5. Clear customer cart
  DB.cart_items = DB.cart_items.filter(i => i.cart_id !== activeCustomerId);

  updateCartBadge();
  closeModal();
  renderProducts();
  updateKPIs();
  renderAdminTable();

  alert(`🎉 Order #${orderId} Placed Successfully!\n\nTotal: ₹${totalAmount.toLocaleString('en-IN')}\nPayment: Completed (${payType})\nTrigger 'reduce_product_stock' automatically decremented inventory in MySQL.`);
}

function closeModal() {
  document.getElementById('modal-overlay').classList.remove('active');
}

// Toast helper
function showToast(msg) {
  let toast = document.getElementById('toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.id = 'toast';
    toast.style.cssText = 'position:fixed;bottom:24px;right:24px;background:#0f172a;color:#fff;padding:10px 18px;border-radius:8px;font-size:13px;z-index:9999;box-shadow:0 4px 12px rgba(0,0,0,0.15);transition:opacity 0.2s ease;';
    document.body.appendChild(toast);
  }
  toast.innerText = msg;
  toast.style.opacity = '1';
  setTimeout(() => { toast.style.opacity = '0'; }, 2500);
}

// Admin Studio Logic
function updateKPIs() {
  const totalRev = DB.orders.reduce((sum, o) => sum + o.total_amount, 0);
  const totalOrders = DB.orders.length;
  const totalCust = DB.customers.length;
  const totalProd = DB.products.length;

  document.getElementById('kpi-rev').innerText = '₹ ' + totalRev.toLocaleString('en-IN', {minimumFractionDigits: 2});
  document.getElementById('kpi-orders').innerText = totalOrders + ' Orders';
  document.getElementById('kpi-cust').innerText = totalCust + ' Customers';
  document.getElementById('kpi-prod').innerText = totalProd + ' Products';
}

function renderAdminTable() {
  const container = document.getElementById('admin-table-container');
  const title = document.getElementById('admin-table-title');
  if (!container || !title) return;

  title.innerText = `Table: ${activeAdminTable.toUpperCase()} (${(DB[activeAdminTable] || []).length} Records)`;

  const data = DB[activeAdminTable];
  if (!data || data.length === 0) {
    container.innerHTML = '<p style="color:var(--text-muted);padding:14px;">No records found in this table.</p>';
    return;
  }

  const columns = Object.keys(data[0]);

  container.innerHTML = `
    <table class="db-table">
      <thead>
        <tr>
          ${columns.map(c => `<th>${c.toUpperCase()}</th>`).join('')}
        </tr>
      </thead>
      <tbody>
        ${data.map(row => `
          <tr>
            ${columns.map(c => `<td>${row[c] !== null && row[c] !== undefined ? row[c] : '-'}</td>`).join('')}
          </tr>
        `).join('')}
      </tbody>
    </table>
  `;
}

function runExperimentQuery(expNum) {
  const consoleEl = document.getElementById('sql-output');
  let sql = '';

  switch (expNum) {
    case 3:
      sql = `-- EXPERIMENT 3: DDL (Data Definition Language)
CREATE TABLE PRODUCT (
  product_id INT AUTO_INCREMENT PRIMARY KEY,
  category_id INT NOT NULL,
  supplier_id INT NOT NULL,
  name VARCHAR(100) NOT NULL,
  description VARCHAR(255),
  stock_qty INT NOT NULL DEFAULT 0,
  price DECIMAL(10,2) NOT NULL,
  CONSTRAINT chk_product_stock CHECK (stock_qty >= 0),
  CONSTRAINT chk_product_price CHECK (price >= 0),
  CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES CATEGORY(category_id),
  CONSTRAINT fk_product_supplier FOREIGN KEY (supplier_id) REFERENCES SUPPLIER(supplier_id)
);
-- 14 tables, PKs, FKs, CHECK constraints, UNIQUE constraints created successfully.`;
      break;
    case 4:
      sql = `-- EXPERIMENT 4: DML (Data Manipulation Language)
INSERT INTO PRODUCT (product_id, category_id, supplier_id, name, description, stock_qty, price) VALUES
(1, 1, 1, 'Laptop', 'Portable computer', 20, 55000.00),
(2, 2, 2, 'T-Shirt', 'Cotton T-shirt', 100, 799.00),
(3, 3, 3, 'SQL Book', 'Database textbook', 50, 499.00);
-- Query OK, 3 rows affected (0.01 sec)`;
      break;
    case 5:
      sql = `-- EXPERIMENT 5: DQL (Data Query Language & Joins)
SELECT o.order_id, c.name AS customer_name, p.name AS product_name, oi.quantity, oi.unit_price, o.total_amount
FROM ORDERS o
JOIN CUSTOMER c ON o.customer_id = c.customer_id
JOIN ORDER_ITEM oi ON o.order_id = oi.order_id
JOIN PRODUCT p ON oi.product_id = p.product_id;

Results:
+----------+---------------+--------------+----------+------------+--------------+
| order_id | customer_name | product_name | quantity | unit_price | total_amount |
+----------+---------------+--------------+----------+------------+--------------+
|        1 | Aarav Sharma  | Laptop       |        1 |   55000.00 |     55000.00 |
|        2 | Priya Patel   | T-Shirt      |        2 |     799.00 |      1598.00 |
|        3 | Rahul Mehta   | SQL Book     |        1 |     499.00 |       499.00 |
+----------+---------------+--------------+----------+------------+--------------+
3 rows in set (0.00 sec)`;
      break;
    case 6:
      sql = `-- EXPERIMENT 6: DCL (Data Control Language - Roles & Privileges)
CREATE USER 'shop_admin'@'localhost' IDENTIFIED BY 'Admin@123';
GRANT ALL PRIVILEGES ON online_shopping.* TO 'shop_admin'@'localhost';

CREATE USER 'customer_user'@'localhost' IDENTIFIED BY 'Cust@123';
GRANT SELECT, INSERT ON online_shopping.ORDERS TO 'customer_user'@'localhost';
GRANT SELECT, INSERT ON online_shopping.ORDER_ITEM TO 'customer_user'@'localhost';
GRANT SELECT ON online_shopping.PRODUCT TO 'customer_user'@'localhost';
FLUSH PRIVILEGES;`;
      break;
    case 7:
      sql = `-- EXPERIMENT 7: Views and Triggers
CREATE OR REPLACE VIEW customer_order_view AS
SELECT c.customer_id, c.name, COUNT(o.order_id) AS total_orders, COALESCE(SUM(o.total_amount), 0) AS total_spent
FROM CUSTOMER c
LEFT JOIN ORDERS o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name;

DELIMITER $$
CREATE TRIGGER reduce_product_stock
AFTER INSERT ON ORDER_ITEM
FOR EACH ROW
BEGIN
  UPDATE PRODUCT
  SET stock_qty = stock_qty - NEW.quantity
  WHERE product_id = NEW.product_id;
END$$
DELIMITER ;`;
      break;
    case 8:
      sql = `-- EXPERIMENT 8: TCL (Transaction Control Language)
START TRANSACTION;
  INSERT INTO ORDERS (order_id, customer_id, total_amount, status) VALUES (6, 1, 55000.00, 'Pending');
  INSERT INTO ORDER_ITEM (order_id, product_id, quantity, unit_price) VALUES (6, 1, 1, 55000.00);
  INSERT INTO PAYMENT (payment_id, order_id, amount, payment_type, status) VALUES (6, 6, 55000.00, 'Credit Card', 'Completed');
COMMIT;
-- Transaction successfully committed (ACID compliant).`;
      break;
  }

  consoleEl.innerText = sql;
}

// Initialization & Event Listeners
document.addEventListener('DOMContentLoaded', () => {
  // Populate customer dropdown
  const custSelect = document.getElementById('customer-select');
  if (custSelect) {
    custSelect.innerHTML = DB.customers.map(c => `
      <option value="${c.customer_id}">${c.name}</option>
    `).join('');
    custSelect.addEventListener('change', e => {
      activeCustomerId = parseInt(e.target.value);
      updateCartBadge();
      showToast(`Shopping as ${DB.customers.find(c => c.customer_id === activeCustomerId)?.name}`);
    });
  }

  // Populate category filter pills
  const pillsContainer = document.getElementById('category-pills');
  if (pillsContainer) {
    let pillsHtml = `<button class="category-pill active" data-cat="all">All Categories</button>`;
    pillsHtml += DB.categories.map(c => `
      <button class="category-pill" data-cat="${c.category_id}">${c.name}</button>
    `).join('');
    pillsContainer.innerHTML = pillsHtml;

    pillsContainer.addEventListener('click', e => {
      if (e.target.classList.contains('category-pill')) {
        document.querySelectorAll('.category-pill').forEach(b => b.classList.remove('active'));
        e.target.classList.add('active');
        currentCategory = e.target.getAttribute('data-cat');
        renderProducts();
      }
    });
  }

  // Search input
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('input', e => {
      searchQuery = e.target.value;
      renderProducts();
    });
  }

  // Sort input
  const sortSelect = document.getElementById('sort-select');
  if (sortSelect) {
    sortSelect.addEventListener('change', e => {
      currentSort = e.target.value;
      renderProducts();
    });
  }

  // View toggle buttons (Customer vs Admin)
  const toggleCust = document.getElementById('toggle-customer');
  const toggleAdmin = document.getElementById('toggle-admin');
  const customerView = document.getElementById('customer-view');
  const adminView = document.getElementById('admin-view');
  const custSelectorWrap = document.getElementById('customer-selector-wrap');
  const cartBtn = document.getElementById('cart-button');

  if (toggleCust && toggleAdmin) {
    toggleCust.addEventListener('click', () => {
      toggleCust.classList.add('active');
      toggleAdmin.classList.remove('active');
      customerView.classList.add('active');
      adminView.classList.remove('active');
      custSelectorWrap.style.display = 'flex';
      cartBtn.style.display = 'flex';
    });

    toggleAdmin.addEventListener('click', () => {
      toggleAdmin.classList.add('active');
      toggleCust.classList.remove('active');
      customerView.classList.remove('active');
      adminView.classList.add('active');
      custSelectorWrap.style.display = 'none';
      cartBtn.style.display = 'none';
      updateKPIs();
      renderAdminTable();
    });
  }

  // Admin sidebar table selection
  const adminNav = document.getElementById('admin-nav');
  if (adminNav) {
    adminNav.addEventListener('click', e => {
      const item = e.target.closest('.admin-nav-item');
      if (item) {
        document.querySelectorAll('.admin-nav-item').forEach(i => i.classList.remove('active'));
        item.classList.add('active');
        activeAdminTable = item.getAttribute('data-table');
        renderAdminTable();
      }
    });
  }

  // Cart button click
  if (cartBtn) {
    cartBtn.addEventListener('click', openCartModal);
  }

  // Modal close
  const modalClose = document.getElementById('modal-close');
  const modalOverlay = document.getElementById('modal-overlay');
  if (modalClose) modalClose.addEventListener('click', closeModal);
  if (modalOverlay) {
    modalOverlay.addEventListener('click', e => {
      if (e.target === modalOverlay) closeModal();
    });
  }

  // Initial renders
  renderProducts();
  updateCartBadge();
  updateKPIs();
  renderAdminTable();
  runExperimentQuery(5);
});

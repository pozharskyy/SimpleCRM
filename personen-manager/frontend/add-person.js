async function doAddPerson() {
      const fields = ['name', 'lastname', 'street', 'postalCode', 'city', 'country'];
      const data = {};

      for (const field of fields) {
        const val = document.getElementById(field).value.trim();
        if (!val) {
          alert('All fields are required. Please fill in: ' + field);
          return;
        }
        data[field] = val;
      }

      const btn = document.getElementById('submitBtn');
      btn.disabled = true;
      btn.textContent = 'Speichern...';

      try {
        const response = await fetch('/api/person', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(data)
        });

        const json = await response.json();

        if (response.ok) {
          showFeedback('success', 'Person added successfully.');
          fields.forEach(f => document.getElementById(f).value = '');
        } else {
          showFeedback('error', 'Error: ' + (json.message || response.statusText));
        }
      } catch (err) {
        showFeedback('error', 'Request failed: ' + err.message);
      } finally {
        btn.disabled = false;
        btn.textContent = 'Add New Person';
      }
    }

    function showFeedback(type, message) {
      const el = document.getElementById('feedback');
      el.className = 'notice notice--' + type;
      el.textContent = message;
      el.style.display = 'block';
    }
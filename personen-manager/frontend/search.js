    document.getElementById('query').addEventListener('keydown', function(e) {
      if (e.key === 'Enter') doSearch();
    });

    async function doSearch() {
      const q = document.getElementById('query').value.trim();
      if (!q) {
        alert('Please enter a search term.');
        return;
      }

      const btn = document.getElementById('searchBtn');
      btn.disabled = true;
      btn.textContent = 'Searching…';
      document.getElementById('feedback').style.display = 'none';
      document.getElementById('results').innerHTML = '';

      try {
        const response = await fetch('/api/search?' + new URLSearchParams({ q }));
        const json = await response.json();

        if (!response.ok) {
          showFeedback('error', 'Error: ' + (json.message || response.statusText));
          return;
        }

        renderResults(json);
      } catch (err) {
        showFeedback('error', 'Request failed: ' + err.message);
      } finally {
        btn.disabled = false;
        btn.textContent = 'Search';
      }
    }

    function renderResults(persons) {
  const container = document.getElementById('results');
  container.innerHTML = ''; // Clear previous results

  if (persons.length === 0) {
    showFeedback('notice', 'No results found.');
    return;
  }

  const table = document.createElement('table');
  table.className = 'results-table';

  table.innerHTML = `
    <thead>
      <tr>
        <th>First Name</th>
        <th>Last Name</th>
        <th>Street</th>
        <th>Postal Code</th>
        <th>City</th>
        <th>Country</th>
        <th>Actions</th>
      </tr>
    </thead>
  `;

  const tbody = document.createElement('tbody');
  for (const p of persons) {
    const tr = document.createElement('tr');

    for (const field of ['name', 'lastname', 'street', 'postalCode', 'city', 'country']) {
      const td = document.createElement('td');
      td.textContent = p[field] ?? '';
      tr.appendChild(td);
    }

    // Add Delete button
    const tdActions = document.createElement('td');
    const deleteBtn = document.createElement('button');
    deleteBtn.textContent = 'Delete';
    deleteBtn.className = 'delete-btn';
    deleteBtn.addEventListener('click', () => deletePerson(p.id, tr));
    tdActions.appendChild(deleteBtn);
    tr.appendChild(tdActions);

    tbody.appendChild(tr);
  }

  table.appendChild(tbody);
  container.appendChild(table);
}

// Function to delete a person
async function deletePerson(personId, rowElement) {
  if (!confirm('Are you sure you want to delete this person?')) return;

  try {
    const response = await fetch('/api/delete', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ id: personId })
    });
    if (!response.ok) {
      const json = await response.json();
      showFeedback('error', 'Error deleting person: ' + (json.message || response.statusText));
      return;
    }

    // Remove row from table
    rowElement.remove();
    showFeedback('success', 'Person deleted successfully.');
  } catch (err) {
    showFeedback('error', 'Request failed: ' + err.message);
  }
}
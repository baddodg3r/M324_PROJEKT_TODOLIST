import { afterEach, beforeEach, describe, expect, jest, test } from '@jest/globals';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../src/App';

describe('App', () => {
	beforeEach(() => {
		global.fetch = jest.fn();
	});

	afterEach(() => {
		jest.resetAllMocks();
	});

	test('sendet einen neuen Task beim Klick auf Absenden an das Backend', async () => {
		const user = userEvent.setup();

		// Der erste fetch passiert beim Laden der App und holt die bestehende Task-Liste.
		global.fetch.mockResolvedValueOnce({
			json: async () => [],
		});

		// Der zweite fetch ist der POST-Request. Er bleibt offen, damit window.location.href nicht ausgeloest wird.
		global.fetch.mockReturnValueOnce(new Promise(() => {}));

		render(<App />);

		await waitFor(() => {
			expect(global.fetch).toHaveBeenCalledWith('http://localhost:8080/');
		});

		await user.type(screen.getByRole('textbox'), 'Frontend Task');
		await user.click(screen.getByRole('button', { name: /absenden/i }));

		await waitFor(() => {
			expect(global.fetch).toHaveBeenCalledTimes(2);
		});

		expect(global.fetch).toHaveBeenNthCalledWith(2, 'http://localhost:8080/tasks', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ taskdescription: 'Frontend Task' }),
		});
	});

	test('löscht einen Task beim Klick auf den Delete-Button', async () => {
		const user = userEvent.setup();

		// Der erste fetch beim App-Load holt die Task-Liste mit einem bestehendem Task
		global.fetch.mockResolvedValueOnce({
			json: async () => [{ taskdescription: 'Existing Task' }],
		});

		// Der zweite fetch ist der DELETE-Request. Er bleibt offen, damit window.location.href nicht ausgeloest wird.
		global.fetch.mockReturnValueOnce(new Promise(() => {}));

		render(<App />);

		// Warte, dass der Task angezeigt wird
		await waitFor(() => {
			expect(screen.getByText(/Existing Task/)).toBeInTheDocument();
		});

		// Klick auf den Delete-Button. Im Frontend wird dafuer das Checkmark-Zeichen "✔" gerendert.
		const deleteButton = screen.getByRole('button', { name: '✔' });
		await user.click(deleteButton);

		// Verifiziere, dass der DELETE-Request korrekt erfolgt ist
		await waitFor(() => {
			expect(global.fetch).toHaveBeenCalledTimes(2);
		});

		expect(global.fetch).toHaveBeenNthCalledWith(2, 'http://localhost:8080/delete', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ taskdescription: 'Existing Task' }),
		});
	});
});

import { afterEach, beforeEach, describe, expect, jest, test } from '@jest/globals';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import App from '../src/App';

describe('App', () => {
	beforeEach(() => {
		// fetch wird gemockt, damit die Tests kein laufendes Backend brauchen.
		global.fetch = jest.fn();
	});

	afterEach(() => {
		// Nach jedem Test werden alle Mock-Aufrufe entfernt, damit Tests sich nicht gegenseitig beeinflussen.
		jest.resetAllMocks();
	});

	test('sendet einen neuen Task beim Klick auf Absenden an das Backend', async () => {
		const user = userEvent.setup();

		// Arrange: Beim Start lädt die App zuerst die bestehende Task-Liste vom Backend.
		// Für diesen Test reicht eine leere Liste als Antwort.
		global.fetch.mockResolvedValueOnce({
			json: async () => [],
		});

		// Arrange: Der zweite fetch ist später der POST-Request zum Erstellen.
		// Das Promise bleibt absichtlich offen, damit die Weiterleitung window.location.href = "/" nicht ausgelöst wird.
		global.fetch.mockReturnValueOnce(new Promise(() => {}));

		render(<App />);

		// Assert: Die App muss beim Rendern die bestehende Task-Liste laden.
		await waitFor(() => {
			expect(global.fetch).toHaveBeenCalledWith('/api/v1/');
		});

		// Act: Der Anwender schreibt einen Tasktext und klickt auf den Absenden-Button.
		await user.type(screen.getByRole('textbox'), 'Frontend Task');
		await user.click(screen.getByRole('button', { name: /absenden/i }));

		// Assert: Nach dem Klick muss ein zweiter fetch-Aufruf für den POST-Request vorhanden sein.
		await waitFor(() => {
			expect(global.fetch).toHaveBeenCalledTimes(2);
		});

		// Assert: Der Absenden-Button schickt den Task an den Backend-Endpunkt POST /api/v1/tasks.
		expect(global.fetch).toHaveBeenNthCalledWith(2, '/api/v1/tasks', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ taskdescription: 'Frontend Task' }),
		});
	});

	test('löscht einen Task beim Klick auf den Delete-Button', async () => {
		const user = userEvent.setup();

		// Arrange: Beim Start liefert das Backend eine Liste mit einem bestehenden Task.
		// Dadurch wird im Frontend eine Task-Zeile mit Delete-Button gerendert.
		global.fetch.mockResolvedValueOnce({
			json: async () => [{ taskdescription: 'Existing Task' }],
		});

		// Arrange: Der zweite fetch ist später der Request zum Löschen.
		// Das Promise bleibt absichtlich offen, damit die Weiterleitung window.location.href = "/" nicht ausgelöst wird.
		global.fetch.mockReturnValueOnce(new Promise(() => {}));

		render(<App />);

		// Assert: Der bestehende Task muss nach dem Laden im Frontend sichtbar sein.
		await waitFor(() => {
			expect(screen.getByText(/Existing Task/)).toBeInTheDocument();
		});

		// Act: Der Anwender klickt auf den Delete-Button.
		// Im aktuellen Frontend ist dieser Button mit dem Checkmark-Zeichen "✔" beschriftet.
		const deleteButton = screen.getByRole('button', { name: '✔' });
		await user.click(deleteButton);

		// Assert: Nach dem Klick muss ein zweiter fetch-Aufruf für den Delete-Request vorhanden sein.
		await waitFor(() => {
			expect(global.fetch).toHaveBeenCalledTimes(2);
		});

		// Assert: Der Delete-Button schickt den Task an den Backend-Endpunkt POST /api/v1/delete.
		expect(global.fetch).toHaveBeenNthCalledWith(2, '/api/v1/delete', {
			method: 'POST',
			headers: {
				'Content-Type': 'application/json',
			},
			body: JSON.stringify({ taskdescription: 'Existing Task' }),
		});
	});
});

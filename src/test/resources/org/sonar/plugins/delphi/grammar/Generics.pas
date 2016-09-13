unit Generics;

interface

type
	TMyGeneric<T1> = class
	strict private
		function fn(arg1: T1): Boolean;
	end;

implementation

var
	generic1: TList<Integer>;
	generic2: TList<TList<Integer>>;


function TMyGeneric<T1>.fn(arg1: T1): Boolean;
begin
	Result := True;
end;

initialization
	generic1 := TList<Integer>.Create;
	// issue #25 expression with nested generics
	generic2 := TList<TList<Integer>>.Create;

finalization
	FreeAndNil(generic2);
	FreeAndNil(generic1);
end.

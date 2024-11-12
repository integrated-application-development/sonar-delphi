unit DefaultClassArrayProperties;

interface

implementation

type
  TEnum = (Flarp);

  TFoo = class
    class function GetBaz(I: Integer): Integer; static;
    class property Baz[I: Integer]: Integer read GetBaz; default;
  end;

  TBar = class(TFoo)
    class function GetBaz(S: string): string; overload; static;
    class function GetBaz(E: TEnum): TEnum; overload; static;
    class property Baz[S: string]: string read GetBaz; default;
    class property Baz[E: TEnum]: TEnum read GetBaz; default;
  end;

  TBarClass = class of TBar;

class function TFoo.GetBaz(I: Integer): Integer;
begin
  Result := I;
end;

class function TBar.GetBaz(S: string): string;
begin
  Result := S;
end;

class function TBar.GetBaz(E: TEnum): TEnum;
begin
  Result := E;
end;

function BarClassFunc: TBarClass;
begin
  Result := TBar;
end;

procedure Test;
begin
  var A := TBar.Baz[123];
  var B := TBar.Baz['123'];
  var C := TBar.Baz[Flarp];

  var D := BarClassFunc.Baz[123];
  var E := BarClassFunc.Baz['123'];
  var F := BarClassFunc.Baz[Flarp];
end;


end.
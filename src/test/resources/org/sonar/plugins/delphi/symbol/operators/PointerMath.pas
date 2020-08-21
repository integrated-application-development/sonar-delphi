unit PointerMath;

interface

type
  TFoo = record
  end;

{$POINTERMATH ON}
  PFoo = ^TFoo; 
{$POINTERMATH OFF}
  
implementation

procedure ConsumeInteger(Int: Integer);
begin
  // Do nothing
end;

procedure ConsumePointer(Foo: PFoo); overload;
begin
  // Do nothing
end;

procedure ConsumePointer(Bar: PChar); overload;
begin
  // Do nothing
end;

procedure ConsumePointer(Baz: PAnsiChar); overload;
begin
  // Do nothing
end;

procedure Test(Glomp: array of Char);
var
  Foo: PFoo;
  Bar: PChar;
  Baz: PAnsiChar;
  Flarp: array[1..15] of AnsiChar;
begin
  ConsumePointer(Foo + 1);
  ConsumePointer(1 + Foo);
  ConsumePointer(Foo + Foo);
  ConsumePointer(Foo + Bar);
  ConsumePointer(Foo + Glomp);
  ConsumePointer(Foo + Flarp);
  ConsumePointer(Foo - 1);
  ConsumeInteger(Foo - Bar);
  ConsumeInteger(Foo - Glomp);
  ConsumeInteger(Foo - Flarp);

  ConsumePointer(Bar + 1);
  ConsumePointer(1 + Bar);
  ConsumePointer(Bar + Bar);
  ConsumePointer(Bar + Baz);
  ConsumePointer(Bar + Glomp);
  ConsumePointer(Bar + Flarp);
  ConsumePointer(Bar - 1);
  ConsumeInteger(Bar - Foo);
  ConsumeInteger(Bar - Baz);
  ConsumeInteger(Bar - Glomp);
  ConsumeInteger(Bar - Flarp);
  
  ConsumePointer(Baz + 1);
  ConsumePointer(1 + Baz);
  ConsumePointer(Baz + Baz);
  ConsumePointer(Baz + Foo);
  ConsumePointer(Baz + Glomp);
  ConsumePointer(Baz + Flarp);
  ConsumePointer(Baz - 1);
  ConsumeInteger(Baz - Bar);
  ConsumeInteger(Baz - Foo);
  ConsumeInteger(Baz - Glomp);
  ConsumeInteger(Baz - Flarp);
end;

end.